package com.sweetshop.sweetshop_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetshop.sweetshop_backend.service.SweetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@WebMvcTest(SweetController.class)
public class SweetPurchaseTestController {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SweetService sweetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testPurchaseSweetSuccess() throws Exception {
        String sweetId = "123";
        int quantity = 5;
        
        when(sweetService.purchaseSweet(sweetId, quantity)).thenReturn(true);

        Map<String, Object> request = new HashMap<>();
        request.put("quantity", quantity);

        mockMvc.perform(post("/api/sweets/{id}/purchase", sweetId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Purchase successful"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sweetId").value("123"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    @WithMockUser
    void testPurchaseSweetFailed() throws Exception {
        String sweetId = "999";
        int quantity = 5;
        
        when(sweetService.purchaseSweet(sweetId, quantity)).thenReturn(false);

        Map<String, Object> request = new HashMap<>();
        request.put("quantity", quantity);

        mockMvc.perform(post("/api/sweets/{id}/purchase", sweetId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Purchase failed - Sweet not found or insufficient stock"))
                .andExpect(jsonPath("$.success").value(false));
    }

}