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
        String testSentence = """
                              Federal investigators seized multiple documents labeled \u201csecret,\u201d \u201cconfidential,\u201d and \u201cclassified,\u201d including some about weapons of mass destruction, during the late August search of the office of President Donald Trump\u2019s former national security adviser John Bolton, according to newly released court records that are part of an investigation into whether Bolton mishandled defense records.
                              The new details provide additional insight into what evidence federal investigators have collected as they look at possible criminal charges for Bolton, who became a political antagonist of Trump\u2019s after he published a book in summer 2020 about how the president bungled several interactions with and about foreign governments.
                              A manuscript Bolton wrote for the book, titled \u201cThe Room Where It Happened,\u201d initially contained classified details, federal officials told Bolton at the time, but he hasn\u2019t been charged with any crime.
                              During the recent search of Bolton\u2019s office, agents found a binder about a State Department security briefing for the 2000-2001 administration transition, plus documents about a communications plan, the US Mission to the United Nations, travel and weapons of mass destruction, according to an inventory list that was filed in court and obtained by media outlets this week. CNN and other news organizations had sued for access to the records about the search.
                              The inventory list written by FBI agents specifically said the property removed from Bolton\u2019s Washington, DC, office on August 22 included: \u201cTravel memo documents with pages labeled secret\u201d; \u201cUS Mission to the United Nations - Confidential Documents\u201d; \u201cU.S. Government Strategic Communications Plan - Confidential Documents\u201d; and \u201cConfidential Documents with (redacted) heading Weapons of Mass Destruction Classified Documents.\u201d
                              Confidential and Secret are both levels of classification for national security information.
                              The federal agents also took four computers and a USB flash drive from Bolton\u2019s office, according to the records.
                              According to a person familiar with Bolton\u2019s office, the paper documents Bolton had there were not from the Trump administration. Instead, they may have been 20 years old or older \u2013 from his earlier tenures in the federal government when he was an under secretary at the State Department and the United Nations ambassador during the second Bush administration.
                              The age of the documents may be important, given that it\u2019s possible they had been declassified by the federal government over the years, the person said, speaking anonymously because the investigation is ongoing. Bolton, 76, has maintained a private office since 2014 for political and policy work, according to public filings.
                              Bolton\u2019s lawyer Abbe Lowell has repeatedly said in recent public statements responding to the investigative activity that the records Bolton had would have been typical of those kept by a long-time government official.
                              \u201cThe documents with classification markings from the period 1998 - 2006 date back to Amb. Bolton\u2019s time in the George W. Bush Administration,\u201d Lowell said in a statement on Wednesday. \u201cAn objective and thorough review will show nothing inappropriate was stored or kept by Ambassador Bolton.\u201d
                              Yet the Justice Department confirmed in court filings this week the sensitive grand jury investigation is ongoing. The documents say investigators are looking at possible violations of criminal statutes that prohibit the unauthorized retention of classified records and improper handling of national defense material.
                              Court proceedings prompted by media outlets, including CNN, previously made public other records about the investigation around Bolton and the search of his home, which occurred on the same day that federal authorities searched his office.
                              The agents at the house had removed several phones, computers and drives, a binder labeled \u201cstatements and reflections to allied strikes \u2026\u201d and folders of documents labeled \u201cTrump I-IV,\u201d those court records said.
                              Federal investigators said they had reason to believe they would find classified records in the search of his house in Bethesda, Maryland.
                              His interactions with a classification expert in the federal government to remove classified details from his book manuscript in 2020 were part of the FBI\u2019s reasoning for the search, as was a foreign adversary\u2019s hack of his AOL account. CNN previously reported that the US intelligence community believes Bolton\u2019s emails may have been intercepted by China, Russia or Iran.
                              """ ;
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


