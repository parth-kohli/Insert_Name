package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Integer> {

    // For: searchNewsArticles(query)
    @Query("SELECT n FROM NewsArticle n WHERE " +
            "LOWER(n.headline) LIKE LOWER(CONCAT('% ', :query, ' %')) OR " +
            "LOWER(n.description) LIKE LOWER(CONCAT('% ', :query, ' %')) OR " +
            "LOWER(n.article) LIKE LOWER(CONCAT('% ', :query, ' %')) " +
            "ORDER BY " +
            "CASE " +
            "WHEN LOWER(n.headline) LIKE LOWER(CONCAT('% ', :query, ' %')) THEN 0 " +
            "ELSE 1 " +
            "END ASC, " +
            "n.date DESC")
    List<NewsArticle> searchByKeyword(@Param("query") String query, Pageable pageable);

    List<NewsArticle> findTop20ByCategoryIgnoreCaseOrderByDateDesc(String category);
    @Query(value = "SELECT * FROM NewsArticles WHERE DATE(date) = :targetDate", nativeQuery = true)
    List<NewsArticle> findByDateString(@Param("targetDate") String targetDate);
}