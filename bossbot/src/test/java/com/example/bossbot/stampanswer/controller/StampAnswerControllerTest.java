package com.example.bossbot.stampanswer.controller;

import com.example.bossbot.stampanswer.dto.CreateStampAnswerRequest;
import com.example.bossbot.stampanswer.dto.UpdateStampAnswerRequest;
import com.example.bossbot.stampanswer.entity.StampAnswer;
import com.example.bossbot.stampanswer.repository.StampAnswerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StampAnswerController.
 * Uses @SpringBootTest for full application context and H2 in-memory database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("StampAnswerController REST API Integration Tests")
@WithMockUser
class StampAnswerControllerTest {

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StampAnswerRepository stampAnswerRepository;

    private MockMvc mockMvc;
    private StampAnswer testEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testEntity = StampAnswer.builder()
                .question("What is Spring Boot?")
                .keywords("spring, boot, framework")
                .answer("Spring Boot is a Java framework")
                .isActive(true)
                .usageCount(10)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedBy(1L)
                .build();
    }

    @AfterEach
    void tearDown() {
        stampAnswerRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/stamp-answers - Should create stamp answer successfully")
    void testCreate_Success() throws Exception {
        CreateStampAnswerRequest createRequest = CreateStampAnswerRequest.builder()
                .question("New Test Question")
                .keywords("new, test")
                .answer("New Test Answer")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/stamp-answers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.question", is("New Test Question")))
                .andExpect(jsonPath("$.keywords", is("new, test")))
                .andExpect(jsonPath("$.answer", is("New Test Answer")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    @DisplayName("POST /api/v1/stamp-answers - Should return 400 when validation fails")
    void testCreate_ValidationFails() throws Exception {
        CreateStampAnswerRequest invalidRequest = CreateStampAnswerRequest.builder().build();

        mockMvc.perform(post("/api/v1/stamp-answers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers/{id} - Should return stamp answer by ID")
    void testGetById_Success() throws Exception {
        StampAnswer saved = stampAnswerRepository.save(testEntity);

        mockMvc.perform(get("/api/v1/stamp-answers/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.question", is("What is Spring Boot?")))
                .andExpect(jsonPath("$.answer", is("Spring Boot is a Java framework")));
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers/{id} - Should return 400 when not found")
    void testGetById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/stamp-answers/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers - Should return all stamp answers")
    void testGetAll_Success() throws Exception {
        stampAnswerRepository.save(testEntity);

        mockMvc.perform(get("/api/v1/stamp-answers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].question", is("What is Spring Boot?")));
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers?activeOnly=true - Should return only active answers")
    void testGetAll_ActiveOnly() throws Exception {
        stampAnswerRepository.save(testEntity);
        
        StampAnswer inactive = StampAnswer.builder()
                .question("Inactive Question")
                .answer("Inactive Answer")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedBy(1L)
                .build();
        stampAnswerRepository.save(inactive);

        mockMvc.perform(get("/api/v1/stamp-answers")
                        .param("activeOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isActive", is(true)));
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers/search - Should search stamp answers")
    void testSearch_Success() throws Exception {
        stampAnswerRepository.save(testEntity);

        mockMvc.perform(get("/api/v1/stamp-answers/search")
                        .param("q", "spring")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].question", containsString("Spring")));
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers/by-question - Should return answer by exact question")
    void testGetByQuestion_Success() throws Exception {
        stampAnswerRepository.save(testEntity);

        mockMvc.perform(get("/api/v1/stamp-answers/by-question")
                        .param("q", "What is Spring Boot?")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question", is("What is Spring Boot?")))
                .andExpect(jsonPath("$.answer", is("Spring Boot is a Java framework")));
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers/by-question - Should return 404 when question not found")
    void testGetByQuestion_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/stamp-answers/by-question")
                        .param("q", "Non-existent question")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/stamp-answers/most-used - Should return most used answers")
    void testGetMostUsed_Success() throws Exception {
        testEntity.setUsageCount(100);
        stampAnswerRepository.save(testEntity);
        
        StampAnswer lowUsage = StampAnswer.builder()
                .question("Low usage question")
                .answer("Low usage answer")
                .isActive(true)
                .usageCount(5)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedBy(1L)
                .build();
        stampAnswerRepository.save(lowUsage);

        mockMvc.perform(get("/api/v1/stamp-answers/most-used")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].usageCount", is(100)));
    }

    @Test
    @DisplayName("PUT /api/v1/stamp-answers/{id} - Should update stamp answer successfully")
    void testUpdate_Success() throws Exception {
        StampAnswer saved = stampAnswerRepository.save(testEntity);
        
        UpdateStampAnswerRequest updateRequest = UpdateStampAnswerRequest.builder()
                .question("Updated question")
                .keywords("updated, keywords")
                .build();

        mockMvc.perform(put("/api/v1/stamp-answers/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.question", is("Updated question")))
                .andExpect(jsonPath("$.keywords", is("updated, keywords")));
    }

    @Test
    @DisplayName("PUT /api/v1/stamp-answers/{id} - Should return 400 when updating non-existent answer")
    void testUpdate_NotFound() throws Exception {
        UpdateStampAnswerRequest updateRequest = UpdateStampAnswerRequest.builder()
                .question("Updated question")
                .build();

        mockMvc.perform(put("/api/v1/stamp-answers/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("DELETE /api/v1/stamp-answers/{id} - Should delete stamp answer successfully")
    void testDelete_Success() throws Exception {
        StampAnswer saved = stampAnswerRepository.save(testEntity);

        mockMvc.perform(delete("/api/v1/stamp-answers/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/stamp-answers/{id} - Should return 400 when deleting non-existent answer")
    void testDelete_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/stamp-answers/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("POST /api/v1/stamp-answers/{id}/usage - Should record usage successfully")
    void testRecordUsage_Success() throws Exception {
        StampAnswer saved = stampAnswerRepository.save(testEntity);
        int initialUsageCount = saved.getUsageCount();

        mockMvc.perform(post("/api/v1/stamp-answers/" + saved.getId() + "/usage")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        StampAnswer updated = stampAnswerRepository.findById(saved.getId()).orElseThrow();
        assert updated.getUsageCount() == initialUsageCount + 1;
    }

    @Test
    @DisplayName("POST /api/v1/stamp-answers/{id}/usage - Should return 400 when stamp answer not found")
    void testRecordUsage_NotFound() throws Exception {
        mockMvc.perform(post("/api/v1/stamp-answers/999/usage")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testSearch_EmptyResults() throws Exception {
        mockMvc.perform(get("/api/v1/stamp-answers/search")
                        .param("q", "nonexistentsearchterm12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
