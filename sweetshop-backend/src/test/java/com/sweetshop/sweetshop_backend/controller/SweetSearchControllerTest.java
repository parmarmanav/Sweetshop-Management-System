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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

@WebMvcTest(SweetController.class)
public class SweetSearchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SweetService sweetService;

    @Test
    @WithMockUser
    void testSearchSweetsByName() throws Exception {
        // Arrange
        List<Sweet> chocolateSweets = Arrays.asList(
            new Sweet("Dark Chocolate", "", 0, 0),
            new Sweet("Milk Chocolate", "", 0, 0)
        );

        when(sweetService.searchSweetsByName("chocolate")).thenReturn(chocolateSweets);

        // Act & Assert
        mockMvc.perform(get("/api/sweets/search")
                .param("name", "chocolate"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Dark Chocolate"))
                .andExpect(jsonPath("$[1].name").value("Milk Chocolate"));
    }

   @Test
    @WithMockUser
    void testSearchSweetsByCategory() throws Exception {

        Sweet gulab = new Sweet("Gulab Jamun", "Indian Sweet", 5.00, 50);
        gulab.setId("1");
        
        Sweet rasgulla = new Sweet("Rasgulla", "Indian Sweet", 4.50, 60);
        rasgulla.setId("2");

        List<Sweet> indianSweets = Arrays.asList(gulab, rasgulla);

        when(sweetService.searchSweetsByCategory("Indian Sweet")).thenReturn(indianSweets);


        mockMvc.perform(get("/api/sweets/search")
                .param("category", "Indian Sweet"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Indian Sweet"))
                .andExpect(jsonPath("$[1].category").value("Indian Sweet"));
    }

    
    @Test
    @WithMockUser
    void testSearchSweetsByPriceRange() throws Exception {
        // Arrange
        Sweet affordable1 = new Sweet("Candy Bar", "Candy", 1.50, 200);
        affordable1.setId("1");
        
        Sweet affordable2 = new Sweet("Lollipop", "Candy", 0.75, 300);
        affordable2.setId("2");

        List<Sweet> affordableSweets = Arrays.asList(affordable1, affordable2);

        when(sweetService.searchSweetsByPriceRange(0.50, 2.00)).thenReturn(affordableSweets);

        // Act & Assert
        mockMvc.perform(get("/api/sweets/search")
                .param("minPrice", "0.50")
                .param("maxPrice", "2.00"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].price").value(1.50))
                .andExpect(jsonPath("$[1].price").value(0.75));
    }

    @Test
    @WithMockUser
    void testSearchSweetsMultipleParams() throws Exception {
        // Arrange
        Sweet premiumChocolate = new Sweet("Premium Dark Chocolate", "Chocolate", 15.00, 25);
        premiumChocolate.setId("1");

        List<Sweet> premiumChocolates = Arrays.asList(premiumChocolate);

        when(sweetService.searchSweets("chocolate", "Chocolate", 10.00, 20.00))
                .thenReturn(premiumChocolates);

        // Act & Assert
        mockMvc.perform(get("/api/sweets/search")
                .param("name", "chocolate")
                .param("category", "Chocolate")
                .param("minPrice", "10.00")
                .param("maxPrice", "20.00"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Premium Dark Chocolate"))
                .andExpect(jsonPath("$[0].category").value("Chocolate"))
                .andExpect(jsonPath("$[0].price").value(15.00));
    }
    
}