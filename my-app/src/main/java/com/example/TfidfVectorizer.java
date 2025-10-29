package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Converts text documents into numerical TF-IDF vectors based on term frequency and inverse document frequency.
public class TfidfVectorizer {
    private Map<String, Double> idfScores;
    private Set<String> vocabulary;
    private List<String> vocabularyList;

    //Constructor: Initializes the data structures
    public TfidfVectorizer() {
        this.idfScores = new HashMap<>();
        this.vocabulary = new HashSet<>();
    }

    public void fit(List<String> corpus) {
        Map<String, Integer> docFreq = new HashMap<>();     //Word → number of docs containing it
        int docCount = corpus.size();                       //Total number of documents

        for (String doc : corpus) {
            String[] tokens = Preprocessor.Stemming(doc);   //Tokenize, clean and stem the document
            Set<String> uniqueTokens = new HashSet<>();     //Unique words per document

            //Add words to vocabulary and track their presence
            for (String token : tokens) {
                vocabulary.add(token);
                uniqueTokens.add(token);
            }

            //Count in how many documents each word appears
            for (String token : uniqueTokens) {
                docFreq.put(token, docFreq.getOrDefault(token, 0) + 1);
            }
        }

        //Calculate IDF for every word: IDF = log(totalDocs / (docsContainingWord + 1)) + 1
        for (String word : vocabulary) {
            int numDocsContainingWord = docFreq.getOrDefault(word, 0);
            double idf = Math.log((double) docCount / (numDocsContainingWord + 1)) + 1;
            idfScores.put(word, idf);
        }
        this.vocabularyList = new ArrayList<>(this.vocabulary);
    }

    //Combines TF (term frequency in document) and precomputed IDF
    public Map<String, Double> transform(String document) {
        //Tokenize, clean and stem the document
        String[] tokens = Preprocessor.Stemming(document);

        //Calculate TF and initializing map to store TF-IDF results
        Map<String, Double> tfScores = calculateTf(tokens);
        Map<String, Double> tfidfVector = new HashMap<>();

        //For words in vocabulary, multiply TF * IDF
        for (String token : tokens) {
            if (vocabulary.contains(token)) {
                double tf = tfScores.get(token);
                double idf = idfScores.getOrDefault(token, 1.0);
                tfidfVector.put(token, tf * idf);
            }
        }
        //Returns Map ADT
        return tfidfVector;
    }

    //Converts a document into a fixed-length double array where each position represents a word from the vocabulary
    public double[] transformToVector(String document) {
        //Get TF-IDF Map
        Map<String, Double> tfidfMap = this.transform(document);

        double[] vector = new double[this.vocabularyList.size()];

        //Fill vector based on vocabulary order
        for (int i = 0; i < this.vocabularyList.size(); i++) {
            String word = this.vocabularyList.get(i);
            vector[i] = tfidfMap.getOrDefault(word, 0.0);
        }
        return vector;
    }

    //Calculate Term Frequency: TF = (count of word in doc) / (total number of words in doc)
    private Map<String, Double> calculateTf(String[] tokens) {
        Map<String, Double> tfScores = new HashMap<>();
        Map<String, Integer> wordCount = new HashMap<>();
        int totalTerms = tokens.length;
        if (totalTerms == 0) {
            return tfScores;
        }

        //Count occurrences of each token using a HashMap
        for (String token : tokens) {
            wordCount.put(token, wordCount.getOrDefault(token, 0) + 1);
        }

        //Divide each count by total number of tokens to normalize
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            tfScores.put(entry.getKey(), (double) entry.getValue() / totalTerms);
        }

        //Return final TF Map
        return tfScores;
    }
}