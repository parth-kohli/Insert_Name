package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        ModelTrainer.TrainedModels trainedModels = ModelTrainer.trainModels();
        List<Article> allArticles= loadTrialArticles();
        fetchMultipleArticles.vectorizer = trainedModels.vectorizer;
        fetchMultipleArticles.model = trainedModels.classifier;
        System.out.println("Calculating Bias Scores... ");
        for (int i = 0; i<allArticles.size(); i++){
            String textToAnalyze;
            Article article = allArticles.get(i);
            String title= article.title;
            String fullArticleText = article.content;
            String description = article.description;
            if (fullArticleText != null && !fullArticleText.isBlank()) {
                textToAnalyze = title + " " + description + " " + fullArticleText;
            } else {
                System.out.println("   ! Scrape failed, falling back to snippet for: " + title);
                textToAnalyze = title + " " + description;
            }
            Map<String, Double> biasScores = fetchMultipleArticles.calculateBiasScores(textToAnalyze);
            article.leftBias =  biasScores.getOrDefault("Left", 0.0).floatValue();
            article.rightBias= biasScores.getOrDefault("Right", 0.0).floatValue();
            article.centerBias=  biasScores.getOrDefault("Center", 0.0).floatValue();
            allArticles.remove(i);
            allArticles.add(i, article);
        }

        List<List<Article>> clusters = fetchMultipleArticles.clusterArticles(allArticles);
        System.out.println("Processing " + clusters.size() + " clusters to concatenate categories...");
        for (List<Article> cluster : clusters) {
            if (cluster == null || cluster.isEmpty()) {
                continue;
            }
            Set<String> categoriesInCluster = new LinkedHashSet<>();
            for (Article article : cluster) {
                if (article.category != null && !article.category.isBlank()) {
                    String[] individualCategories = article.category.split(", ");
                    for (String cat : individualCategories) {
                        if (cat != null && !cat.isBlank()) {
                            categoriesInCluster.add(cat.trim());
                        }
                    }
                }
            }
            String concatenatedCategories = String.join(", ", categoriesInCluster);
            cluster.get(0).category = concatenatedCategories;
        }
        System.out.println("Sorting " + clusters.size() + " clusters using custom Merge Sort...");
        List<List<Article>> sortedClusters = fetchMultipleArticles.mergeSort(clusters);
        sortedClusters=sortedClusters.stream().filter(cluster -> cluster.size() > 1).limit(10).collect(Collectors.toList());
        Article article;
        for (int i= 0; i < sortedClusters.size(); i++){
            System.out.println("\n");
            System.out.println("=".repeat(20));
            System.out.println("Article "+(i+1));
            Article mainArticle = sortedClusters.get(i).get(0);
            System.out.println(mainArticle.title+" ("+mainArticle.sourceName+")");
            System.out.println("L = "+mainArticle.leftBias+" C = "+ mainArticle.centerBias+" R = "+mainArticle.rightBias);
            for (int j = 1; j<sortedClusters.get(i).size(); j++){
                System.out.println("-".repeat(20));
                Article subArticle = sortedClusters.get(i).get(j);
                System.out.println(subArticle.title+" ("+subArticle.sourceName+")");
                System.out.println("L = "+subArticle.leftBias+" C = "+ subArticle.centerBias+" R = "+subArticle.rightBias);

            }

        }

    }
    private static List<Article> loadTrialArticles() {
        try {
            Path path = Paths.get("trialarticles.json");
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
}
