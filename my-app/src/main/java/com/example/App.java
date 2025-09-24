package com.example;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class App {
    public static void loadDataFromDirectory(String directoryPath, List<String> corpus, List<String> labels) {
        File mainDir = new File(directoryPath);
        if (!mainDir.exists() || !mainDir.isDirectory()) {
            System.err.println("Error: Directory not found at " + directoryPath);
            return;
        }
        for (File classDir : mainDir.listFiles()) {
            if (classDir.isDirectory()) {
                if (!(classDir.getName().equals("center") || classDir.getName().equals("left") || classDir.getName().equals("right"))) continue;
                String label =  classDir.getName();
                if (label == null) continue;
                for (File textFile : classDir.listFiles()) {
                    try {
                        String content = Files.readString(textFile.toPath());
                        corpus.add(content);
                        labels.add(label);
                    } catch (IOException e) {
                        System.err.println("Failed to read file: " + textFile.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        TfidfVectorizer vectorizer = new TfidfVectorizer();
        List<String> trainingCorpus = new ArrayList<>();
        List<String> trainingLabels = new ArrayList<>();
        String trainingDataPath = "C:\\Users\\parth\\Desktop\\Parthyo\\News\\Insert_Name\\model_trainer"; 
        loadDataFromDirectory(trainingDataPath, trainingCorpus, trainingLabels);
        
        System.out.println("Loaded " + trainingCorpus.size() + " documents.");
        if (trainingCorpus.isEmpty()) {
            System.out.println("No training data found. Please check the '" + trainingDataPath + "' directory.");
            return;
        }
        vectorizer.fit(trainingCorpus);
        List<double[]> trainingVectors = new ArrayList<>();
        for (String doc : trainingCorpus) {
            trainingVectors.add(vectorizer.transformToVector(doc));
        }
        MultinomialNaiveBayes classifier = new MultinomialNaiveBayes();
        classifier.fit(trainingVectors, trainingLabels);
        System.out.println();
        String testSentence = "";
        System.out.println("Raw sentence: \"" + testSentence + "\"");
        double[] testVector = vectorizer.transformToVector(testSentence);
        String predictedClass = classifier.predict(testVector);
        System.out.println("Predicted Class: " + predictedClass);
        Map<String, Double> probabilities = classifier.predict_proba(testVector);

        System.out.println("\nConfidence Scores:");
        probabilities.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed()) 
            .forEach(entry -> {
                System.out.printf("  - %s: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
            });


        
        
    }
}


