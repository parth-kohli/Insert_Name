package com.example;

import java.util.HashMap;
import java.util.Map;

public class StopWordTrie {

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
    }
    private final TrieNode root = new TrieNode();
    public void insert(String word) {
        TrieNode node = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            node = node.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        node.isEndOfWord = true;
    }
    public boolean contains(String word) {
        TrieNode node = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }
}
