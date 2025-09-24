package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TfidfVectorizer {
    private Map<String, Double> idfScores;
    private Set<String> vocabulary;
    private List<String> vocabularyList;

    public TfidfVectorizer() {
        this.idfScores = new HashMap<>();
        this.vocabulary = new HashSet<>();
    }
    
    public void fit(List<String> corpus) {
        Map<String, Integer> docFreq = new HashMap<>();
        int docCount = corpus.size();
        List<String[]> processedCorpus = new ArrayList<>();
        for (String doc : corpus) {
            String[] tokens = Preprocessor.Stemming(doc);
            processedCorpus.add(tokens);
            Set<String> uniqueTokens = new HashSet<>();
            for (String token : tokens) {
                vocabulary.add(token);
                uniqueTokens.add(token);
            }
            for (String token : uniqueTokens) {
                docFreq.put(token, docFreq.getOrDefault(token, 0) + 1);
            }
        }
        for (String word : vocabulary) {
            int numDocsContainingWord = docFreq.getOrDefault(word, 0);
            double idf = Math.log((double) docCount / (numDocsContainingWord + 1)) + 1;
            idfScores.put(word, idf);
           
        }
    
        this.vocabularyList = new ArrayList<>(this.vocabulary);
        
        
    }

    public Map<String, Double> transform(String document) {
        String[] tokens = Preprocessor.Stemming(document);
        Map<String, Double> tfScores = calculateTf(tokens);
        Map<String, Double> tfidfVector = new HashMap<>();
        for (String token : tokens) {
            if (vocabulary.contains(token)) {
                double tf = tfScores.get(token);
                double idf = idfScores.getOrDefault(token, 1.0);
                tfidfVector.put(token, tf * idf);
            }
        }
        return tfidfVector;
    }

    public double[] transformToVector(String document) {
        Map<String, Double> tfidfMap = this.transform(document);
        double[] vector = new double[this.vocabularyList.size()];
        for (int i = 0; i < this.vocabularyList.size(); i++) {
            String word = this.vocabularyList.get(i);
            vector[i] = tfidfMap.getOrDefault(word, 0.0);
        }
        return vector;
    }
    
    private Map<String, Double> calculateTf(String[] tokens) {
        Map<String, Double> tfScores = new HashMap<>();
        Map<String, Integer> wordCount = new HashMap<>();
        int totalTerms = tokens.length;
        if (totalTerms == 0) {
            return tfScores; 
        }
        for (String token : tokens) {
            wordCount.put(token, wordCount.getOrDefault(token, 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            tfScores.put(entry.getKey(), (double) entry.getValue() / totalTerms);
        }
        return tfScores;
    }
}