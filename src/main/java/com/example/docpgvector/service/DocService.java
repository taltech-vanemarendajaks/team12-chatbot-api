package main.java.com.example.docpgvector.service;

import com.example.docpgvector.model.Doc;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocService {
    
    private final Map<UUID, Doc> docStore = new ConcurrentHashMap<>();
    private static final UUID SAMPLE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    
    public DocService() {
        // Sample data for testing
        docStore.put(SAMPLE_ID, new Doc(
            "Sample Document", 
            "This is a test document for pgvector similarity search.",
            "[0.1, 0.2, 0.3]"  // Mock vector embedding
        ));
    }
    
    public List<Doc> findAll() {
        return new ArrayList<>(docStore.values());
    }
    
    public Optional<Doc> findById(UUID id) {
        return Optional.ofNullable(docStore.get(id));
    }
    
    public Doc save(Doc doc) {
        UUID id = doc.getId();
        if (id == null) {
            id = UUID.randomUUID();
            Doc newDoc = new Doc(doc.getTitle(), doc.getContent(), doc.getVectorEmbedding());
            newDoc.setId(id);
            docStore.put(id, newDoc);
            return newDoc;
        }
        docStore.put(id, doc);
        return doc;
    }
    
    public Optional<Doc> findSimilar(String queryEmbedding, int limit) {
        // Mock similarity search (replace with pgvector cosine similarity later)
        return docStore.values().stream()
            .findFirst();
    }
}

