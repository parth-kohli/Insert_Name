package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BiasedArticleRepository extends JpaRepository<BiasedArticle, Integer> {

    //List of functions for BiasedArticles
    List<BiasedArticle> findByArticleId(Integer articleId);
}