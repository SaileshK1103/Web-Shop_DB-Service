package com.webshop;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentContainsShopEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Web Shop DB Service API"))
                .andExpect(jsonPath("$.paths", hasKey("/api/categories")))
                .andExpect(jsonPath("$.paths", hasKey("/api/products")))
                .andExpect(jsonPath("$.paths", hasKey("/api/orders")));
    }

    @Test
    void openApiDocumentDescribesTheOrderEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/orders'].post.summary").value("Place an order"))
                .andExpect(jsonPath("$.paths['/api/orders'].post.tags[0]").value("Orders"))
                .andExpect(jsonPath("$.paths['/api/orders'].post.responses", hasKey("201")))
                .andExpect(jsonPath("$.paths['/api/orders'].post.responses", hasKey("409")));
    }

    @Test
    void swaggerUiIsAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}