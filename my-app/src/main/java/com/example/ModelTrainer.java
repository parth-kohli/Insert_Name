package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ModelTrainer {
    static String parentDir = new File(System.getProperty("user.dir")).getParent();

    private static final String TRAINING_DATA_PATH = parentDir + "/model_trainer";


    public static class TrainedModels {
        final TfidfVectorizer vectorizer;
        final MultinomialNaiveBayes classifier;

        TrainedModels(TfidfVectorizer vectorizer, MultinomialNaiveBayes classifier) {
            this.vectorizer = vectorizer;
            this.classifier = classifier;
        }
    }

    public static TrainedModels trainModels() {
        TfidfVectorizer vectorizer = new TfidfVectorizer();
        MultinomialNaiveBayes classifier = new MultinomialNaiveBayes();

        List<String> trainingCorpus = new ArrayList<>();
        List<String> trainingLabels = new ArrayList<>();

        System.out.println("Loading training data from: " + TRAINING_DATA_PATH);
        loadDataFromDirectory(TRAINING_DATA_PATH, trainingCorpus, trainingLabels);
        System.out.println("Loaded " + trainingCorpus.size() + " training documents.");

        if (trainingCorpus.isEmpty()) {
            System.err.println("CRITICAL: No training data found. Please check the path.");
            return new TrainedModels(vectorizer, classifier);
        }

        System.out.println("Fitting TfidfVectorizer...");
        vectorizer.fit(trainingCorpus);

        System.out.println("Transforming training data to vectors...");
        List<double[]> trainingVectors = new ArrayList<>();
        for (String doc : trainingCorpus) {
            trainingVectors.add(vectorizer.transformToVector(doc));
        }

        System.out.println("Fitting MultinomialNaiveBayes classifier...");
        classifier.fit(trainingVectors, trainingLabels);

        System.out.println("ML Models are trained and ready.");
        return new TrainedModels(vectorizer, classifier);
    }

    private static void loadDataFromDirectory(String directoryPath, List<String> corpus, List<String> labels) {
        File mainDir = new File(directoryPath);
        if (!mainDir.exists() || !mainDir.isDirectory()) {
            System.err.println("Error: Directory not found at " + directoryPath);
            return;
        }

        for (File classDir : mainDir.listFiles()) {
            if (classDir.isDirectory()) {
                String label = classDir.getName().toLowerCase();
                if (!(label.equals("center") || label.equals("left") || label.equals("right"))) continue;

                File[] files = classDir.listFiles();
                if (files == null) continue;

                for (File textFile : files) {
                    try {
                        String content = Files.readString(textFile.toPath());
                        corpus.add(content);
                        labels.add(label);
                    } catch (IOException e) {
                        System.err.println("Failed to read file: " + textFile.getAbsolutePath());
                    }
                }
            }
        }
    }
}
