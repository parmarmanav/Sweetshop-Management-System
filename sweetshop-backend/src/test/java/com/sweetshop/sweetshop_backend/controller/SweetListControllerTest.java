package com.sweetshop.sweetshop_backend.controller;

import com.sweetshop.sweetshop_backend.model.Sweet;
import com.sweetshop.sweetshop_backend.service.SweetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

@WebMvcTest(SweetController.class)
public class SweetListControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SweetService sweetService;

    @Test
    @WithMockUser
    void testGetAllSweetsSuccess() throws Exception {
        Sweet sweet1 = new Sweet("Chocolate Bar", "Candy", 2.50, 100);
        sweet1.setId("1");
        when(sweetService.getAllSweets()).thenReturn(Arrays.asList(sweet1));

        mockMvc.perform(get("/api/sweets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Chocolate Bar"));
    }

}
