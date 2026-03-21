package com.example.docpgvector.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Doc {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "vector_embedding", columnDefinition = "vector(1536)")
    private String vectorEmbedding;  // pgvector column
    
    private Integer similarityScore;
    
    // Default constructor for JPA
    public Doc() {}
    
    public Doc(String title, String content, String vectorEmbedding) {
        this.title = title;
        this.content = content;
        this.vectorEmbedding = vectorEmbedding;
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getVectorEmbedding() { return vectorEmbedding; }
    public void setVectorEmbedding(String vectorEmbedding) { 
        this.vectorEmbedding = vectorEmbedding; 
    }
    
    public Integer getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Integer similarityScore) { 
        this.similarityScore = similarityScore; 
    }
}