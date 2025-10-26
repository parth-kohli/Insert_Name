package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class fetchMultipleArticles {
    private static final String API_KEY = "e8efcdd3703b4e58b9b3f207e4591d95"; // Use your own API key
    private static final String CACHE_FILE = "cached_articles"+LocalDate.now().toString()+".json";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Set<String> STOP_WORDS = Set.of("a", "an", "the", "in", "on", "at", "for", "with", "is", "are", "was", "were", "to", "of", "and", "says", "posts", "after", "by");
    private static final Map<String, String> DOMAIN_TO_SOURCE_ID = Map.ofEntries(
            Map.entry("apnews.com", "associated-press"), Map.entry("reuters.com", "reuters"),
            Map.entry("bbc.com", "bbc-news"), Map.entry("wsj.com", "the-wall-street-journal"),
            Map.entry("theguardian.com", "the-guardian-uk"), Map.entry("msnbc.com", "msnbc"),
            Map.entry("foxnews.com", "fox-news"), Map.entry("huffpost.com", "the-huffington-post"),
            Map.entry("vox.com", "vox"), Map.entry("breitbart.com", "breitbart-news"),
            Map.entry("newsweek.com", "newsweek"), Map.entry("cbsnews.com", "cbs-news"),
            Map.entry("theblaze.com", "the-blaze")
    );

    private static TfidfVectorizer vectorizer;
    private static MultinomialNaiveBayes model;
    public static List<List<Article>> fetchAndClusterArticles(ModelTrainer.TrainedModels trainedModels) {
        fetchMultipleArticles.vectorizer = trainedModels.vectorizer;
        fetchMultipleArticles.model = trainedModels.classifier;
        String topicQuery = "(politics OR government OR election OR congress OR court OR world)";
        List<String> domainsToSearch = Arrays.asList("apnews.com", "msnbc.com", "foxnews.com", "reuters.com", "huffpost.com", "nypost.com", "bbc.com", "theguardian.com", "wsj.com", "cbsnews.com");
        System.out.println("🔎 Checking for cached articles at '" + CACHE_FILE + "'...");
        List<Article> allArticles = loadArticlesFromCache();
        if (allArticles.isEmpty()) {
            System.out.println("   Cache not found or empty. Fetching from API...");
            allArticles = fetchAllArticles(domainsToSearch, 100, topicQuery);
            saveArticlesToCache(allArticles);
            System.out.println("📰 Created a pool of " + allArticles.size() + " articles and saved to cache.");
        } else {
            System.out.println("📰 Loaded " + allArticles.size() + " articles from cache.");
        }
        if (allArticles.isEmpty()) {
            System.out.println("Could not fetch or load any valid articles.");
            return Collections.emptyList();
        }
        System.out.println("\nClustering articles to find specific events...");
        List<List<Article>> clusters = clusterArticles(allArticles);
        System.out.println("Sorting " + clusters.size() + " clusters using custom Merge Sort...");
        List<List<Article>> sortedClusters = mergeSort(clusters);
        return sortedClusters.stream()
                .filter(cluster -> cluster.size() > 1)
                .limit(10)
                .collect(Collectors.toList());
    }

    private static String scrapeArticle(String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(10000).get(); 
            Element articleBody = doc.selectFirst("article, div.article-body, div.article-content, main, [role=main]");
            if (articleBody != null) {
                return articleBody.text().replaceAll("\\s+", " "); 
            }
            return null;
        } catch (Exception e) {
            System.err.println("   ! Scrape failed for " + url + ": " + e.getMessage());
            return null;
        }
    }

    private static Map<String, Double> calculateBiasScores(String text) {
        if (vectorizer == null || model == null || text == null || text.isBlank()) {
            return Map.of("Left", 0.33, "Center", 0.34, "Right", 0.33);
        }
        double[] vector = vectorizer.transformToVector(text);
        Map<String, Double> probs = model.predict_proba(vector);
        probs.putIfAbsent("left", 0.0);
        probs.putIfAbsent("center", 0.0);
        probs.putIfAbsent("right", 0.0);
        
        return Map.of(
            "Left", probs.get("left"),
            "Center", probs.get("center"),
            "Right", probs.get("right")
        );
    }


    private static List<Article> fetchAllArticles(List<String> domains, int countPerSource, String topicQuery) {
        Set<Article> articleSet = new LinkedHashSet<>();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);

        for (String domain : domains) {
            try {
                String queryParam = DOMAIN_TO_SOURCE_ID.containsKey(domain) ? "&sources=" + DOMAIN_TO_SOURCE_ID.get(domain) : "&domains=" + domain;
                String url = "https://newsapi.org/v2/everything?q=" + topicQuery
                        + "&from=" + yesterdayStr + "&to=" + yesterdayStr
                        + queryParam + "&pageSize=" + countPerSource + "&language=en&apiKey=" + API_KEY;

                String responseBody = sendRequest(url);
                JSONObject json = new JSONObject(responseBody);

                if (!"ok".equals(json.optString("status"))) continue;

                JSONArray articles = json.getJSONArray("articles");

                for (int i = 0; i < articles.length(); i++) {
                    JSONObject articleJson = articles.getJSONObject(i);
                    String title = articleJson.optString("title");
                    if (title == null || title.isBlank() || "[Removed]".equals(title) || title.length() < 25) {
                        continue;
                    }

                    String description = articleJson.optString("description");
                    String imageUrl = articleJson.optString("urlToImage");
                    String publishedAt = articleJson.getString("publishedAt");
                    String sourceName = articleJson.getJSONObject("source").getString("name");
                    String articleUrl = articleJson.getString("url");
                    System.out.println("   -> Scraping: " + title);
                    String fullArticleText = scrapeArticle(articleUrl);
                    
                    String textToAnalyze;
                    String contentForDB;

                    if (fullArticleText != null && !fullArticleText.isBlank()) {
                        textToAnalyze = title + " " + description + " " + fullArticleText;
                        contentForDB = fullArticleText;
                    } else {
                        System.out.println("   ! Scrape failed, falling back to snippet for: " + title);
                        String apiSnippet = articleJson.optString("content");
                        textToAnalyze = title + " " + description + " " + apiSnippet;
                        contentForDB = (apiSnippet != null && !apiSnippet.isBlank()) ? apiSnippet : "No content available.";
                    }
                    Map<String, Double> biasScores = calculateBiasScores(textToAnalyze);
                    articleSet.add(new Article(
                            title, articleUrl, sourceName, publishedAt,
                            description, imageUrl, 
                            contentForDB,
                            biasScores.getOrDefault("Left", 0.0).floatValue(),
                            biasScores.getOrDefault("Center", 0.0).floatValue(),
                            biasScores.getOrDefault("Right", 0.0).floatValue()
                    ));
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch from " + domain + ". Error: " + e.getMessage());
            }
        }
        return new ArrayList<>(articleSet);
    }

    
    private static void saveArticlesToCache(List<Article> articles) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(articles);
            Files.writeString(Paths.get(CACHE_FILE), json);
        } catch (IOException e) {
            System.err.println("Error saving articles to cache: " + e.getMessage());
        }
    }

    private static List<Article> loadArticlesFromCache() {
        try {
            Path path = Paths.get(CACHE_FILE);
            if (!Files.exists(path)) return Collections.emptyList();
            String json = Files.readString(path);
            if (json == null || json.isBlank()) return Collections.emptyList();
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Article>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (IOException e) {
            System.err.println("Error loading articles from cache: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static List<List<Article>> clusterArticles(List<Article> articles) {
        List<List<Article>> clusters = new ArrayList<>();
        Set<Article> clusteredArticles = new HashSet<>();

        for (Article articleA : articles) {
            if (clusteredArticles.contains(articleA)) continue;
            List<Article> newCluster = new ArrayList<>();
            newCluster.add(articleA);
            clusteredArticles.add(articleA);
            for (Article articleB : articles) {
                if (!articleA.equals(articleB) && !clusteredArticles.contains(articleB) && isTitleSimilar(articleA.title, articleB.title)) {
                    newCluster.add(articleB);
                    clusteredArticles.add(articleB);
                }
            }
            clusters.add(newCluster);
        }
        return clusters;
    }

    private static boolean isTitleSimilar(String title1, String title2) {
        final double ENTITY_KEYWORD_THRESHOLD = 0.20;
        final double PURE_KEYWORD_THRESHOLD = 0.4;
        Set<String> multiWordEntities1 = getEntities(title1, "\\b[A-Z][a-z]+(?:\\s[A-Z][a-z]+){1,2}\\b");
        Set<String> multiWordEntities2 = getEntities(title2, "\\b[A-Z][a-z]+(?:\\s[A-Z][a-z]+){1,2}\\b");
        Set<String> singleWordEntities1 = getEntities(title1, "\\b[A-Z][a-z]{3,}\\b");
        Set<String> singleWordEntities2 = getEntities(title2, "\\b[A-Z][a-Z]{3,}\\b");
        String normTitle1 = normalizeTitle(title1);
        String normTitle2 = normalizeTitle(title2);
        Set<String> keywords1 = getKeywords(normTitle1);
        Set<String> keywords2 = getKeywords(normTitle2);
        boolean entitiesMatch = !Collections.disjoint(multiWordEntities1, multiWordEntities2) ||
                !Collections.disjoint(singleWordEntities1, singleWordEntities2);
        double keywordSimilarity = calculateJaccardSimilarity(keywords1, keywords2);
        if (entitiesMatch && keywordSimilarity >= ENTITY_KEYWORD_THRESHOLD) return true;
        return keywordSimilarity >= PURE_KEYWORD_THRESHOLD;
    }

    private static String normalizeTitle(String title) {
        return title.toLowerCase()
                .replace("one", "1").replace("two", "2").replace("three", "3")
                .replace("four", "4").replace("five", "5").replace("six", "6")
                .replace("seven", "7").replace("eight", "8").replace("nine", "9")
                .replace("ten", "10");
    }

    private static Set<String> getKeywords(String normalizedText) {
        return Arrays.stream(normalizedText.replaceAll("[^a-z0-9\\s]", "").split("\\s+"))
                .filter(word -> word.length() > 2 && !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    private static Set<String> getEntities(String originalCaseTitle, String regex) {
        return Pattern.compile(regex)
                .matcher(originalCaseTitle)
                .results()
                .map(mr -> mr.group().toLowerCase())
                .collect(Collectors.toSet());
    }

    private static double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    private static String sendRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static int getSourceDiversity(List<Article> cluster) {
        if (cluster == null || cluster.isEmpty()) return 0;
        return (int) cluster.stream().map(a -> a.sourceName).distinct().count();
    }

private static List<List<Article>> mergeSort(List<List<Article>> list) {
    if (list.size() <= 1) return list;
    int middle = list.size() / 2;
    List<List<Article>> leftHalf = new ArrayList<>(list.subList(0, middle));
    List<List<Article>> rightHalf = new ArrayList<>(list.subList(middle, list.size()));
    List<List<Article>> sortedLeft = mergeSort(leftHalf);
    List<List<Article>> sortedRight = mergeSort(rightHalf);
    return merge(sortedLeft, sortedRight);
}

private static List<List<Article>> merge(List<List<Article>> left, List<List<Article>> right) {
    List<List<Article>> result = new ArrayList<>();
    int leftIndex = 0;
    int rightIndex = 0;
    while (leftIndex < left.size() && rightIndex < right.size()) {
        List<Article> leftCluster = left.get(leftIndex);
        List<Article> rightCluster = right.get(rightIndex);
        int leftDiversity = getSourceDiversity(leftCluster);
        int rightDiversity = getSourceDiversity(rightCluster);
        if (leftDiversity > rightDiversity) {
            result.add(leftCluster);
            leftIndex++;
        } else if (leftDiversity < rightDiversity) {
            result.add(rightCluster);
            rightIndex++;
        } else {
            if (leftCluster.size() >= rightCluster.size()) {
                result.add(leftCluster);
                leftIndex++;
            } else {
                result.add(rightCluster);
                rightIndex++;
            }
        }
    }
    while (leftIndex < left.size()) {
        result.add(left.get(leftIndex));
        leftIndex++;
    }
    while (rightIndex < right.size()) {
        result.add(right.get(rightIndex));
        rightIndex++;
    }
    return result;
}
}



