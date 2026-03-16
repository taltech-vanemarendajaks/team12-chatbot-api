#!/bin/bash
# Plan [document-mvc].sh - Complete Mac Terminal MVC Setup (your docpgvector project)
# Usage: ./Plan[document-mvc].sh

echo "🚀 Creating complete Spring Boot MVC + pgvector Documents app..."

# 1. Generate fresh project
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,thymeleaf,postgresql,springdoc-openapi-starter-webmvc-ui \
  -d javaVersion=17 \
  -d groupId=com.example \
  -d artifactId=docpgvector \
  -d name=DocPgVector \
  -o docpgvector.zip

unzip -o docpgvector.zip && cd docpgvector

# 2. Create MVC folder structure
mkdir -p src/main/java/com/example/docpgvector/{controller,model,repository,service}
mkdir -p src/main/resources/templates/documents
mkdir -p src/test/java/com/example/docpgvector/controller

# 3. PostgreSQL + pgvector (Docker)
docker run -d --name pgvector -p 5432:5432 -e POSTGRES_PASSWORD=pass postgres:16
docker exec pgvector psql -U postgres -c "CREATE EXTENSION vector;"
docker exec pgvector psql -U postgres -c "CREATE DATABASE docdb;"

# 4. application.yml
cat > src/main/resources/application.yml << 'EOF'
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/docdb
    username: postgres
    password: pass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  sql:
    init:
      mode: always
EOF

# 5. Document Entity (model/)
cat > src/main/java/com/example/docpgvector/model/Document.java << 'EOF'
package com.example.docpgvector.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "text")
    private String content;
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public Document() {}
    // getters/setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    // ... etc
}
EOF

# 6. MVC Controller
cat > src/main/java/com/example/docpgvector/controller/DocumentController.java << 'EOF'
package com.example.docpgvector.controller;

import com.example.docpgvector.model.Document;
import com.example.docpgvector.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/documents")
public class DocumentController {
    @Autowired private DocumentRepository repo;
    
    @GetMapping
    public String list(Model model) {
        model.addAttribute("documents", repo.findAll());
        return "documents/list";
    }
}
EOF

# 7. Repository
cat > src/main/java/com/example/docpgvector/repository/DocumentRepository.java << 'EOF'
package com.example.docpgvector.repository;

import com.example.docpgvector.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {}
EOF

# 8. Thymeleaf template
cat > src/main/resources/templates/documents/list.html << 'EOF'
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Documents</title></head>
<body>
    <h1>Documents (pgvector)</h1>
    <table border="1">
        <tr><th>ID</th><th>Title</th><th>Content</th></tr>
        <tr th:each="doc : ${documents}">
            <td th:text="${doc.id}"></td>
            <td th:text="${doc.title}"></td>
            <td th:text="${doc.content}"></td>
        </tr>
    </table>
    <a href="/documents/new">+ New</a>
</body>
</html>
EOF

# 9. Build & Run
echo "✅ Setup complete! Building..."
mvn clean package -DskipTests

echo "🌐 Starting MVC app..."
mvn spring-boot:run &

sleep 10

echo "✅ Visit:"
echo "  MVC HTML: http://localhost:8080/documents"
echo "  Swagger:  http://localhost:8080/swagger-ui.html"
echo ""
echo "🛑 Stop: Ctrl+C or 'docker stop pgvector'"
