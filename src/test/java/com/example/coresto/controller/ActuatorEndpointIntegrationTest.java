package com.example.coresto.controller;

import com.example.coresto.base.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActuatorEndpointIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /actuator/health returns 200 UP without authentication")
    void testHealthEndpoint_Unauthenticated_ReturnsUp() throws Exception {
        performGet("/actuator/health")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health/liveness probe returns 200 UP for container orchestration")
    void testLivenessProbe_ReturnsUp() throws Exception {
        performGet("/actuator/health/liveness")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health/readiness probe returns 200 UP for container orchestration")
    void testReadinessProbe_ReturnsUp() throws Exception {
        performGet("/actuator/health/readiness")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
