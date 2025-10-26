package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InsertClustersIntoDB {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/News?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "password";

    private static final String SQL_INSERT_NEWS_ARTICLE =
            "INSERT INTO NewsArticles (headline, description, article, imageUrl, date, centerBias, leftBias, rightBias, source) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_BIASED_ARTICLE =
            "INSERT INTO BiasedArticles (headline, article, description, imageUrl, date, category, centerBias, leftBias, rightBias, source, articleId) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static void main(String[] args) {
        try {
            ModelTrainer.TrainedModels trainedModels = ModelTrainer.trainModels();
            List<List<Article>> clusters = fetchMultipleArticles.fetchAndClusterArticles(trainedModels);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                conn.setAutoCommit(false);
                insertClusters(conn, clusters);
                conn.commit();
            }

        } catch (SQLException e) {
            System.err.println("Database connection or transaction failed.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during the pipeline.");
            e.printStackTrace();
        }
    }

    private static void insertClusters(Connection conn, List<List<Article>> clusters) throws SQLException {
        try (PreparedStatement psNews = conn.prepareStatement(SQL_INSERT_NEWS_ARTICLE, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psBiased = conn.prepareStatement(SQL_INSERT_BIASED_ARTICLE)) {

            int eventCounter = 1;
            for (List<Article> cluster : clusters) {
                if (cluster.isEmpty()) continue;

                Article mainArticle = cluster.get(0);
                System.out.printf("\nInserting Event #%d: '%s'\n", eventCounter++, mainArticle.title);
                System.out.printf("   -> Scores: [L:%.2f, C:%.2f, R:%.2f]\n",
                    mainArticle.leftBias, mainArticle.centerBias, mainArticle.rightBias);
                setStatementParams(psNews, mainArticle);
                psNews.executeUpdate();
                int mainArticleId = -1;
                try (ResultSet generatedKeys = psNews.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        mainArticleId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating news article failed, no ID obtained.");
                    }
                }
                for (int i = 1; i < cluster.size(); i++) {
                    Article biasedArticle = cluster.get(i);
                    setStatementParams(psBiased, biasedArticle);
                    psBiased.setInt(11, mainArticleId);
                    psBiased.executeUpdate();
                }
                System.out.println("   -> Saved " + (cluster.size() - 1) + " related (biased) articles.");
            }
        }
    }


    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isBlank() && !value.equals("null")) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }

    private static void setNullableFloat(PreparedStatement ps, int index, Float value) throws SQLException {
        if (value != null) {
            ps.setFloat(index, value);
        } else {
            ps.setNull(index, Types.FLOAT);
        }
    }

    private static void setStatementParams(PreparedStatement ps, Article article) throws SQLException {
        ps.setString(1, article.title);
        setNullableString(ps, 2, article.description);

        if (article.content != null && !article.content.isBlank() && !article.content.equals("null")) {
            ps.setString(3, article.content);
        } else {
            ps.setString(3, "No content snippet available.");
        }
        setNullableString(ps, 4, article.imageUrl);
        ZonedDateTime zdt = ZonedDateTime.parse(article.publishedAt, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime ldt = zdt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        ps.setTimestamp(5, Timestamp.valueOf(ldt));
        setNullableFloat(ps, 7, article.centerBias);
        setNullableFloat(ps, 8, article.leftBias);
        setNullableFloat(ps, 9, article.rightBias);
        setNullableString(ps, 10, article.sourceName);
    }
}



