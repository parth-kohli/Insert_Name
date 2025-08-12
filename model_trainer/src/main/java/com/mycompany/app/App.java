package com.mycompany.app;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class App {
    private static final String API_KEY = "13fe1245885b47bb8fb44650cfd6f53e"; 
    private static final String BASE_URL = "https://newsapi.org/v2/everything";
    private static final Map<String, List<String>> BIAS_SOURCES = new HashMap<>() {{
        put("left", Arrays.asList("theatlantic.com", "salon.com", "alternet.org", "huffpost.com", "theguardian.com","theintercept.com", "slate.com", "democracynow.org", "thedailybeast.com", "motherjones.com", "msnbc.com", "vox.com"));
        put("center", Arrays.asList("fortune.com","theglobeandmail.com","scrippsnews.com","newsweek.com","cbsnews.com", "reuters.com", "bbc.com", "apnews.com","forbes.com", "newsnationnow.com", "reason.com", "wsj.com"));
        put("right", Arrays.asList("americanthinker.com" ,"theamericanconservative.com","freebeacon.com", "wnd.com", "foxnews.com", "nypost.com", "washingtontimes.com", "theblaze.com", "breitbart.com", "spectator.org","thefederalist.com","dailywire.com", "dailycaller.com", "newsmax.com"));
    }};

    public static void main(String[] args) {
        Map<String, List<String>> articlesByBias = new HashMap<>();

        for (String bias : BIAS_SOURCES.keySet()) {
            try {
                Files.createDirectories(Paths.get(bias));
            } catch (IOException e) {
                e.printStackTrace();
            }

            articlesByBias.put(bias, new ArrayList<>());

            int fileCounter = 1;
            for (String source : BIAS_SOURCES.get(bias)) {
                try {
                    System.out.println("Fetching 100 articles from: " + source);

                    String apiUrl = BASE_URL +
                            "?domains=" + source +
                            "&pageSize=100" +
                            "&language=en" +
                            "&apiKey=" + API_KEY;

                    String jsonResponse = fetch(apiUrl);
                    JSONObject obj = new JSONObject(jsonResponse);

                    if (!obj.has("articles")) {
                        System.err.println("⚠ No articles found for " + source);
                        continue;
                    }

                    JSONArray articles = obj.getJSONArray("articles");
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        String title = article.optString("title", "").replaceAll("[\\\\/:*?\"<>|]", "");
                        String url = article.optString("url", "");
                        if (title.isEmpty() || url.isEmpty()) continue;
                        System.out.println("Fetching article: " + title);
                        String content = title + "\n" + scrapeArticle(url);
                        articlesByBias.get(bias).add(content);
                        Files.write(Paths.get(bias, 851+fileCounter + ".txt"), content.getBytes());
                        fileCounter++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (String bias : articlesByBias.keySet()) {
            System.out.println(bias + ": " + articlesByBias.get(bias).size() + " articles collected & saved.");
        }
    }

    private static String fetch(String apiUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static String scrapeArticle(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element articleBody = doc.selectFirst("article, div.article-body, div.article-content, main");
            if (articleBody != null) {
                return articleBody.text();
            }
            return "Could not extract content from: " + url;
        } catch (IOException e) {
            return "Error fetching article: " + e.getMessage();
        }
    }
}
