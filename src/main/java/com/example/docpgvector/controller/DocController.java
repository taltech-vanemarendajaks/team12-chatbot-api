package main.java.com.example.docpgvector.controller;

import com.example.docpgvector.model.Doc;
import com.example.docpgvector.service.DocService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/docs")
public class DocController {
    
    private final DocService docService;
    
    public DocController(DocService docService) {
        this.docService = docService;
    }
    
    @GetMapping
    public List<Doc> getAllDocs() {
        return docService.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Doc> getDocById(@PathVariable Long id) {
        return docService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Doc createDoc(@RequestBody Doc doc) {
        return docService.save(doc);
    }
}
