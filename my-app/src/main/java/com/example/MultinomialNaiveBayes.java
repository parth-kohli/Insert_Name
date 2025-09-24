package com.example;

import java.util.*;

public class MultinomialNaiveBayes {
    private Map<String, Double> classPriors;                  
    private Map<String, Map<Integer, Double>> featureLogProb; 
    private Set<String> classes;
    private int nFeatures;
    private double alpha = 1.0;
    public MultinomialNaiveBayes() {
        this.classPriors = new HashMap<>();
        this.featureLogProb = new HashMap<>();
    }

    public void fit(List<double[]> data, List<String> labels) {
        int nSamples = data.size();
        if (nSamples == 0) return;
        nFeatures = data.get(0).length;

        Map<String, List<double[]>> dataByClass = new HashMap<>();
        for (int i = 0; i < nSamples; i++) {
            dataByClass.computeIfAbsent(labels.get(i), k -> new ArrayList<>()).add(data.get(i));
        }
        this.classes = dataByClass.keySet();

        for (String label : classes) {
            classPriors.put(label, (double) dataByClass.get(label).size() / nSamples);
        }

        for (String label : classes) {
            List<double[]> classData = dataByClass.get(label);

            double[] featureCounts = new double[nFeatures];
            double totalCount = 0.0;
            for (double[] sample : classData) {
                for (int j = 0; j < nFeatures; j++) {
                    featureCounts[j] += sample[j];
                }
            }
            for (double count : featureCounts) totalCount += count;

            Map<Integer, Double> logProbs = new HashMap<>();
            for (int j = 0; j < nFeatures; j++) {
                double prob = (featureCounts[j] + alpha) / (totalCount + alpha * nFeatures);
                logProbs.put(j, Math.log(prob));
            }
            featureLogProb.put(label, logProbs);
        }
    }

    public String predict(double[] vector) {
        String bestClass = null;
        double maxLogProb = Double.NEGATIVE_INFINITY;

        for (String label : classes) {
            double logProb = Math.log(classPriors.get(label));
            Map<Integer, Double> logProbs = featureLogProb.get(label);

            for (int j = 0; j < nFeatures; j++) {
                if (vector[j] > 0) {
                    logProb += vector[j] * logProbs.get(j);
                }
            }

            if (logProb > maxLogProb) {
                maxLogProb = logProb;
                bestClass = label;
            }
        }
        return bestClass;
    }

    public Map<String, Double> predict_proba(double[] vector) {
        Map<String, Double> logScores = new HashMap<>();

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


        double maxLog = Collections.max(logScores.values());
        double sumExp = 0.0;
        Map<String, Double> probs = new HashMap<>();
        for (String label : classes) {
            double expVal = Math.exp(logScores.get(label) - maxLog);
            probs.put(label, expVal);
            sumExp += expVal;
        }
        for (String label : classes) {
            probs.put(label, probs.get(label) / sumExp);
        }
        return probs;
    }
}
