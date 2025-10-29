package com.example;

import java.util.*;

//It uses TF-IDF or term-frequency vectors as input and learns class-conditional probabilities for classification
public class MultinomialNaiveBayes {
    private Map<String, Double> classPriors;                       //Stores prior probability of each class label: P(class)
    private Map<String, Map<Integer, Double>> featureLogProb;      //Nested map: for each class → (feature index → log P(feature | class))
    private Set<String> classes;                                   //Unique class labels (ADT: Set ensures no duplicates)
    private int nFeatures;                                         //Number of features (vocabulary size)

    private double alpha = 1.0;                                    //Laplace smoothing constant (prevents zero probability issues)

    //Constructor: Initializes the ADT used (Maps)
    public MultinomialNaiveBayes() {
        this.classPriors = new HashMap<>();
        this.featureLogProb = new HashMap<>();
    }

    //Trains the Naive Bayes model
    public void fit(List<double[]> data, List<String> labels) {
        int nSamples = data.size();
        if (nSamples == 0) return;
        nFeatures = data.get(0).length;

        //Group all documents by their class label
        //ADT used: Map
        Map<String, List<double[]>> dataByClass = new HashMap<>();
        for (int i = 0; i < nSamples; i++) {
            dataByClass.computeIfAbsent(labels.get(i), k -> new ArrayList<>()).add(data.get(i));
        }

        //Extract all class names
        this.classes = dataByClass.keySet();

        //Compute class prior probabilities, P(class) = (#documents_in_class / total_documents)
        for (String label : classes) {
            classPriors.put(label, (double) dataByClass.get(label).size() / nSamples);
        }

        //Compute conditional probabilities, P(feature | class)
        for (String label : classes) {
            List<double[]> classData = dataByClass.get(label);

            double[] featureCounts = new double[nFeatures];
            double totalCount = 0.0;

            //Sum up all term counts across documents of the same class
            for (double[] sample : classData) {
                for (int j = 0; j < nFeatures; j++) {
                    featureCounts[j] += sample[j];
                }
            }

            //Compute total count of all features
            for (double count : featureCounts) totalCount += count;

            //Apply Laplace smoothing and take logarithm to prevent underflow
            //P(feature_j | class) = (featureCount_j + α) / (totalCount + α * nFeatures)
            Map<Integer, Double> logProbs = new HashMap<>();
            for (int j = 0; j < nFeatures; j++) {
                double prob = (featureCounts[j] + alpha) / (totalCount + alpha * nFeatures);
                logProbs.put(j, Math.log(prob));
            }
            featureLogProb.put(label, logProbs);
        }
    }

    //Predicts the most likely class label for a given document vector
    public String predict(double[] vector) {
        String bestClass = null;
        double maxLogProb = Double.NEGATIVE_INFINITY;

        //For each class, compute log P(class) + Σ [x_j * log P(feature_j | class)]
        for (String label : classes) {
            double logProb = Math.log(classPriors.get(label));
            Map<Integer, Double> logProbs = featureLogProb.get(label);

            for (int j = 0; j < nFeatures; j++) {
                if (vector[j] > 0) {
                    logProb += vector[j] * logProbs.get(j);
                }
            }
            //Select the class with the highest total log probability
            if (logProb > maxLogProb) {
                maxLogProb = logProb;
                bestClass = label;
            }
        }
        return bestClass;
    }

    //Returns normalized class probabilities for a given document
    public Map<String, Double> predict_proba(double[] vector) {
        Map<String, Double> logScores = new HashMap<>();

        //Compute log-probabilities for all classes
        for (String label : classes) {
            double logProb = Math.log(classPriors.get(label));
            Map<Integer, Double> logProbs = featureLogProb.get(label);

            for (int j = 0; j < nFeatures; j++) {
                if (vector[j] > 0) {
                    logProb += vector[j] * logProbs.get(j);
                }
            }
            logScores.put(label, logProb);
        }

        //Use log-sum-exp to prevent numerical underflow
        double maxLog = Collections.max(logScores.values());
        double sumExp = 0.0;
        Map<String, Double> probs = new HashMap<>();

        //Exponentiate and normalize
        for (String label : classes) {
            double expVal = Math.exp(logScores.get(label) - maxLog);
            probs.put(label, expVal);
            sumExp += expVal;
        }

        //Normalize so that all probabilities add up to 1
        for (String label : classes) {
            probs.put(label, probs.get(label) / sumExp);
        }

        //Return final probabilities for each class
        return probs;
    }
}