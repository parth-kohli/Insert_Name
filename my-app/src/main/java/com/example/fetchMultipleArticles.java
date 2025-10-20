package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Article {
    final String title;
    final String url;
    final String sourceName;
    final String publishedAt;

    public Article(String title, String url, String sourceName, String publishedAt) {
        this.title = title;
        this.url = url;
        this.sourceName = sourceName;
        this.publishedAt = publishedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Article article = (Article) obj;
        return Objects.equals(url, article.url);
    }
}

public class fetchMultipleArticles {

    private static final String API_KEY = "e8efcdd3703b4e58b9b3f207e4591d95";
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

    public static void main(String[] args) {
        // This is your main control knob. Change this to find different types of news.
        String topicQuery = "(politics OR government OR election OR congress OR court OR world)";
        List<String> domainsToSearch = Arrays.asList("apnews.com", "msnbc.com", "foxnews.com", "reuters.com", "huffpost.com", "nypost.com", "bbc.com", "theguardian.com", "dailywire.com", "wsj.com", "vox.com", "breitbart.com", "cbsnews.com", "slate.com", "dailycaller.com", "newsweek.com", "salon.com", "newsmax.com", "forbes.com", "thedailybeast.com", "theblaze.com", "fortune.com", "motherjones.com", "thefederalist.com", "newsnationnow.com", "theintercept.com", "washingtontimes.com", "scrippsnews.com", "democracynow.org", "wnd.com", "theglobeandmail.com", "theatlantic.com", "freebeacon.com", "reason.com", "alternet.org", "americanthinker.com", "spectator.org", "theamericanconservative.com");

        System.out.println("🔎 Checking for cached articles at '" + CACHE_FILE + "'...");
        List<Article> allArticles = loadArticlesFromCache();

        if (allArticles.isEmpty()) {
            System.out.println("   Cache not found or empty. Fetching from API on topic: '" + topicQuery + "'...");
            allArticles = fetchAllArticles(domainsToSearch, 100, topicQuery);
            saveArticlesToCache(allArticles);
            System.out.println("📰 Created a pool of " + allArticles.size() + " articles and saved to cache.");
        } else {
            System.out.println("📰 Loaded " + allArticles.size() + " articles from cache. Skipping API fetch.");
        }

        if (allArticles.isEmpty()) {
            System.out.println("❌ Could not fetch or load any valid articles.");
            return;
        }

        System.out.println("\n🧠 Clustering articles to find specific events...");
        List<List<Article>> clusters = clusterArticles(allArticles);

        // Sort clusters by the number of unique sources, then by total size as a tie-breaker.
        Comparator<List<Article>> bySourceDiversity = Comparator.comparingInt(c -> (int) c.stream().map(a -> a.sourceName).distinct().count());
        Comparator<List<Article>> byTotalSize = Comparator.comparingInt(List::size);
        clusters.sort(bySourceDiversity.thenComparing(byTotalSize).reversed());

        List<List<Article>> matchedClusters = clusters.stream()
                .filter(cluster -> cluster.size() > 1)
                .collect(Collectors.toList());

        if (!matchedClusters.isEmpty()) {
            System.out.println("\n✅ Found " + matchedClusters.size() + " distinct news events, sorted by best coverage:");
            int eventCounter = 1;
            for (List<Article> cluster : matchedClusters) {
                String topicTitle = cluster.get(0).title;
                long uniqueSourceCount = cluster.stream().map(article -> article.sourceName).distinct().count();
                System.out.printf("\n--- Event #%d (%d articles from %d sources) ---\n", eventCounter++, cluster.size(), uniqueSourceCount);
                System.out.println("   " + topicTitle);
                cluster.forEach(article ->
                        System.out.println("     - [" + article.sourceName + "]: " + article.title)
                );
            }
        } else {
            System.out.println("\n❌ No multi-source events found in the pool.");
        }
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
            if (!Files.exists(path)) {
                return Collections.emptyList();
            }
            String json = Files.readString(path);
            if (json == null || json.isBlank()) {
                return Collections.emptyList();
            }
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Article>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (IOException e) {
            System.err.println("Error loading articles from cache: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static List<Article> fetchAllArticles(List<String> domains, int countPerSource, String topicQuery) {
        Set<Article> articleSet = new LinkedHashSet<>();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);

        for (String domain : domains) {
            try {
                String queryParam = DOMAIN_TO_SOURCE_ID.containsKey(domain) ? "&sources=" + DOMAIN_TO_SOURCE_ID.get(domain) : "&domains=" + domain;
                String url = "https://newsapi.org/v2/everything?q="
                        + "&from=" + yesterdayStr + "&to=" + yesterdayStr
                        + queryParam + "&pageSize=" + countPerSource + "&language=en&apiKey=" + API_KEY;

                String responseBody = sendRequest(url);
                JSONObject json = new JSONObject(responseBody);

                if (!"ok".equals(json.optString("status"))) continue;

                JSONArray articles = json.getJSONArray("articles");

                for (int i = 0; i < articles.length(); i++) {
                    JSONObject articleJson = articles.getJSONObject(i);
                    String title = articleJson.optString("title");
                    if (title == null || title.isBlank() || "[Removed]".equals(title) || title.length() < 25 || title.matches(".*\\d{2}:\\d{2}.*")) {
                        continue;
                    }
                    articleSet.add(new Article(title, articleJson.getString("url"), articleJson.getJSONObject("source").getString("name"), articleJson.getString("publishedAt")));
                    if (i==0){
                        System.out.println(articleJson.getString("urlToImage"));

                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch from " + domain + ". Error: " + e.getMessage());
            }
        }
        return new ArrayList<>(articleSet);
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
        Set<String> singleWordEntities2 = getEntities(title2, "\\b[A-Z][a-z]{3,}\\b");

        String normTitle1 = normalizeTitle(title1);
        String normTitle2 = normalizeTitle(title2);
        Set<String> keywords1 = getKeywords(normTitle1);
        Set<String> keywords2 = getKeywords(normTitle2);

        boolean entitiesMatch = !Collections.disjoint(multiWordEntities1, multiWordEntities2) ||
                !Collections.disjoint(singleWordEntities1, singleWordEntities2);

        double keywordSimilarity = calculateJaccardSimilarity(keywords1, keywords2);

        if (entitiesMatch && keywordSimilarity >= ENTITY_KEYWORD_THRESHOLD) {
            return true;
        }

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
}