package com.sweetshop.sweetshop_backend.controller;

import com.sweetshop.sweetshop_backend.service.SweetService;
import com.sweetshop.sweetshop_backend.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;

@WebMvcTest(SweetController.class)
public class SweetDeleteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SweetService sweetService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void testDeleteSweetSuccess() throws Exception {
        String sweetId = "123";
        String token = "valid-admin-token";
        
        when(jwtUtil.extractUsername(token)).thenReturn("kashyap");
        when(jwtUtil.extractRole(token)).thenReturn("ADMIN");
        when(jwtUtil.validateToken(token, "kashyap")).thenReturn(true);
        when(sweetService.deleteSweet(sweetId)).thenReturn(true);

        mockMvc.perform(delete("/api/sweets/{id}", sweetId)
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testDeleteSweetNotFound() throws Exception {
        String sweetId = "999";
        String token = "valid-admin-token";
        
        when(jwtUtil.extractUsername(token)).thenReturn("kashyap");
        when(jwtUtil.extractRole(token)).thenReturn("ADMIN");
        when(jwtUtil.validateToken(token, "kashyap")).thenReturn(true);
        when(sweetService.deleteSweet(sweetId)).thenReturn(false);

        mockMvc.perform(delete("/api/sweets/{id}", sweetId)
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteSweetNoToken() throws Exception {
        mockMvc.perform(delete("/api/sweets/{id}", "123")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
                
        verify(sweetService, never()).deleteSweet(anyString());
    }

    @Test
    @WithMockUser
    void testDeleteSweetInvalidToken() throws Exception {
        String token = "invalid-token";
        
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(delete("/api/sweets/{id}", "123")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
                
        verify(sweetService, never()).deleteSweet(anyString());
    }

    @Test
    @WithMockUser
    void testDeleteSweetNonAdminUser() throws Exception {
        String token = "valid-user-token";
        
        when(jwtUtil.extractUsername(token)).thenReturn("regularuser");
        when(jwtUtil.extractRole(token)).thenReturn("USER");
        when(jwtUtil.validateToken(token, "regularuser")).thenReturn(true);

        mockMvc.perform(delete("/api/sweets/{id}", "123")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
                
        verify(sweetService, never()).deleteSweet(anyString());
    }

    @Test
    @WithMockUser
    void testDeleteSweetExpiredToken() throws Exception {
        String token = "expired-token";
        
        when(jwtUtil.extractUsername(token)).thenReturn("kashyap");
        when(jwtUtil.extractRole(token)).thenReturn("ADMIN");
        when(jwtUtil.validateToken(token, "kashyap")).thenReturn(false);

        mockMvc.perform(delete("/api/sweets/{id}", "123")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
                
        verify(sweetService, never()).deleteSweet(anyString());
    }

    @Test
    @WithMockUser
    void testDeleteSweetMalformedAuthHeader() throws Exception {
        mockMvc.perform(delete("/api/sweets/{id}", "123")
                .header("Authorization", "InvalidFormat token123")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
                
        verify(sweetService, never()).deleteSweet(anyString());
    }

    @Test
    @WithMockUser
    void testDeleteSweetEmptyAuthHeader() throws Exception {
        mockMvc.perform(delete("/api/sweets/{id}", "123")
                .header("Authorization", "")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
                
        verify(sweetService, never()).deleteSweet(anyString());
    }
}