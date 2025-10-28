package com.example;

import java.util.HashMap;
import java.util.Map;

public class StopWordTrie {
    //Makes a trie node, with each node having a map of children characters and a pointer to children node and a boolean value for if its EOW
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
    }
    private final TrieNode root = new TrieNode();
    //Inserts a word to the map
    public void insert(String word) {
        TrieNode node = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            node = node.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        node.isEndOfWord = true;
    }
    //Checks if a word is present in the trie
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
