package com.tbf.tcms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbf.tcms.service.LevyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LevyControllerValidationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc buildMockMvc() {
        LevyService levyService = Mockito.mock(LevyService.class);
        LevyController controller = new LevyController(levyService);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/levies/{familyId}/payments requires amount and validates positive")
    void recordPayment_validationErrors() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        // missing amount
        mockMvc.perform(post("/api/levies/1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("amount is required"));

        // negative amount
        mockMvc.perform(post("/api/levies/1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("amount must be greater than 0"));
    }
}
