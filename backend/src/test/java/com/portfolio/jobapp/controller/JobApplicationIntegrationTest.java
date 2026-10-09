package com.portfolio.jobapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.jobapp.dto.request.CompanyRequest;
import com.portfolio.jobapp.dto.request.InterviewRequest;
import com.portfolio.jobapp.dto.request.JobApplicationRequest;
import com.portfolio.jobapp.dto.request.RegisterRequest;
import com.portfolio.jobapp.dto.request.UpdateStatusRequest;
import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import com.portfolio.jobapp.entity.enums.InterviewType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JobApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenUserA;
    private String tokenUserB;

    @BeforeEach
    void setUpUsers() throws Exception {
        // Register User A
        RegisterRequest userAReq = new RegisterRequest("User A", "usera@test.com", "password123");
        MvcResult resA = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userAReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonA = objectMapper.readTree(resA.getResponse().getContentAsString());
        tokenUserA = "Bearer " + jsonA.get("token").asText();

        // Register User B
        RegisterRequest userBReq = new RegisterRequest("User B", "userb@test.com", "password123");
        MvcResult resB = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonB = objectMapper.readTree(resB.getResponse().getContentAsString());
        tokenUserB = "Bearer " + jsonB.get("token").asText();
    }

    @Test
    @DisplayName("Complete E2E workflow: company -> application -> status update -> interview -> dashboard -> delete with user isolation")
    void fullApplicationWorkflowAndUserIsolation() throws Exception {
        // 1. User A creates a company
        CompanyRequest companyReq = new CompanyRequest("Amazon", "https://amazon.jobs", "E-Commerce", "Seattle, WA", "AWS Team");
        MvcResult companyRes = mockMvc.perform(post("/api/companies")
                        .header("Authorization", tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Amazon")))
                .andReturn();
        long companyId = objectMapper.readTree(companyRes.getResponse().getContentAsString()).get("id").asLong();

        // 2. User A creates a job application
        JobApplicationRequest appReq = new JobApplicationRequest(
                companyId,
                "SDE II",
                "Seattle, WA",
                "https://amazon.jobs/123",
                ApplicationStatus.APPLIED,
                150000,
                190000,
                LocalDate.now(),
                null,
                "Applied via referral"
        );

        MvcResult appRes = mockMvc.perform(post("/api/applications")
                        .header("Authorization", tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle", is("SDE II")))
                .andExpect(jsonPath("$.status", is("APPLIED")))
                .andExpect(jsonPath("$.company.name", is("Amazon")))
                .andReturn();
        long applicationId = objectMapper.readTree(appRes.getResponse().getContentAsString()).get("id").asLong();

        // 3. User A retrieves applications with status filter
        mockMvc.perform(get("/api/applications?status=APPLIED&page=0&size=10")
                        .header("Authorization", tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].jobTitle", is("SDE II")));

        // User A retrieves applications with status filter that doesn't match
        mockMvc.perform(get("/api/applications?status=REJECTED")
                        .header("Authorization", tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));

        // 4. User A updates application status to INTERVIEW
        UpdateStatusRequest statusReq = new UpdateStatusRequest(ApplicationStatus.INTERVIEW);
        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INTERVIEW")));

        // 5. User A schedules an interview for this application
        InterviewRequest interviewReq = new InterviewRequest(
                InterviewType.TECHNICAL,
                LocalDateTime.now().plusDays(2),
                "John Bar Raiser",
                "Focus on System Design & Data Structures",
                "PENDING"
        );
        mockMvc.perform(post("/api/applications/" + applicationId + "/interviews")
                        .header("Authorization", tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(interviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type", is("TECHNICAL")))
                .andExpect(jsonPath("$.interviewer", is("John Bar Raiser")));

        // 6. User A checks dashboard statistics
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications", is(1)))
                .andExpect(jsonPath("$.interviewCount", is(1)))
                .andExpect(jsonPath("$.interviewRate", is(100.0)))
                .andExpect(jsonPath("$.recentApplications", hasSize(1)));

        // 7. USER ISOLATION CHECK: User B cannot access User A's application
        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", tokenUserB))
                .andExpect(status().isNotFound());

        // User B cannot update User A's application
        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", tokenUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isNotFound());

        // User B cannot delete User A's application
        mockMvc.perform(delete("/api/applications/" + applicationId)
                        .header("Authorization", tokenUserB))
                .andExpect(status().isNotFound());

        // User B's dashboard shows 0 applications
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications", is(0)));

        // 8. User A deletes the application
        mockMvc.perform(delete("/api/applications/" + applicationId)
                        .header("Authorization", tokenUserA))
                .andExpect(status().isNoContent());

        // 9. Confirm application is deleted
        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", tokenUserA))
                .andExpect(status().isNotFound());
    }
}

