package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BiasedArticleRepository extends JpaRepository<BiasedArticle, Integer> {

    // For: fetchBiasedNews(id)
    // Spring Data JPA automatically creates the SQL query from this method name
    List<BiasedArticle> findByArticleId(Integer articleId);
}