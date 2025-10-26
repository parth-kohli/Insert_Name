package com.example;

import java.util.Objects;

public class Article {
    final String title;
    final String url;
    final String sourceName;
    final String publishedAt;
    final String description;
    final String imageUrl;
    final String content;
    String category;
    final Float leftBias;
    final Float centerBias;
    final Float rightBias;

    public Article(String title, String url, String sourceName, String publishedAt,
                   String description, String imageUrl, String content, String category,
                   Float leftBias, Float centerBias, Float rightBias) {
        this.title = title;
        this.url = url;
        this.sourceName = sourceName;
        this.publishedAt = publishedAt;
        this.description = description;
        this.imageUrl = imageUrl;
        this.content = content;
        this.category=category;
        this.leftBias = leftBias;
        this.centerBias = centerBias;
        this.rightBias = rightBias;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Article article = (Article) obj;
        return Objects.equals(url, article.url);
    }
}