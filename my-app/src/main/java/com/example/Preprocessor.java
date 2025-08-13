package com.example;
public class Preprocessor {
    public static void main(String args) {
        //String rawText= headline + " " + body;
        String text = "India and Pakistan both possess nuclear weapons and fought their deadliest battle in decades in May, sparked by an attack on tourists the previous month in Indian Kashmir, which killed 26 civilians.\n" +
                "Randhir Jaiswal, spokesperson for India's foreign ministry, said: \"Nuclear sabre-rattling is Pakistan’s stock-in-trade,\" adding: \"The international community can draw its own conclusions on the irresponsibility inherent in such remarks.\"\n" +
                "He said it was also regrettable that the reported remarks should have been made while in a friendly third country.\n" +
                "In a version of the speech shared by Pakistani security officials, Munir said: \"The (Indian) aggression has brought the region to the brink of a dangerously escalating war, where a bilateral conflict due to any miscalculation will be a grave mistake.\"";

        // Lowercase and split on non-letters
        String[] tokens = text.toLowerCase().split("[^a-z]+");

        for (String token : tokens) {
            if (!token.isEmpty()) {
                System.out.print(token + "   ");
            }
        }
    }
}