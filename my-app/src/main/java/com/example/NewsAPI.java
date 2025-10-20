package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api") // All endpoints will start with /api
public class NewsAPI {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private BiasedArticleRepository biasedArticleRepository;

    // Endpoint for: fetchNewsArticles()
    @GetMapping("/news")
    public List<NewsArticle> getAllNewsArticles() {
        return newsArticleRepository.findAll();
    }

    // Endpoint for: fetchBiasedNews(id)
    @GetMapping("/news/{id}/biased")
    public List<BiasedArticle> getBiasedArticles(@PathVariable Integer id) {
        return biasedArticleRepository.findByArticleId(id);
    }

    // Endpoint for: searchNewsArticles(query)
    @GetMapping("/news/search")
    public List<NewsArticle> searchArticles(@RequestParam String query) {
        if (query.isBlank()) {
            return Collections.emptyList();
        }
        return newsArticleRepository.searchByKeyword(query);
    }

    // Endpoint for: searchCategories(category)
    @GetMapping("/news/category")
    public List<NewsArticle> getArticlesByCategory(@RequestParam String name) {
        return newsArticleRepository.findByCategoryIgnoreCase(name);
    }
    @GetMapping("/news/today")
    public List<NewsArticle> getTodaysNews() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = LocalDate.now().minusDays(1).format(formatter);
        System.out.println(todayStr);
        return newsArticleRepository.findByDateString(todayStr);
    }
}