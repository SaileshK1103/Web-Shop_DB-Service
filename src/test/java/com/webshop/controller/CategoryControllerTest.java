package com.webshop.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.webshop.entity.Product;
import com.webshop.entity.ProductCategory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    private static final int UNKNOWN_ID = 999_999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createReturnsCreatedCategoryWithLocation() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Books", "description": "Printed and digital books"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/categories/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Books"))
                .andExpect(jsonPath("$.description").value("Printed and digital books"));
    }

    @Test
    void createStoresTheCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Books"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categories"))
                .andExpect(jsonPath("$[*].name").value(contains("Books")));
    }

    @Test
    void createWithBlankNameReturnsBadRequestNamingTheField() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createWithTooLongNameReturnsBadRequest() throws Exception {
        String tooLongName = "x".repeat(101);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + tooLongName + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createWithMalformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsCategoriesSortedByName() throws Exception {
        saveCategory("Toys");
        saveCategory("Books");

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Books", "Toys")));
    }

    @Test
    void getByIdReturnsCategory() throws Exception {
        ProductCategory category = saveCategory("Books");

        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    void getByUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(endsWith("was not found")));
    }

    @Test
    void updateReplacesNameAndDescription() throws Exception {
        ProductCategory category = saveCategory("Books");

        mockMvc.perform(put("/api/categories/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Literature", "description": "Novels and poetry"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Literature"))
                .andExpect(jsonPath("$.description").value("Novels and poetry"));
    }

    @Test
    void updateUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/categories/{id}", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Literature"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWithBlankNameReturnsBadRequest() throws Exception {
        ProductCategory category = saveCategory("Books");

        mockMvc.perform(put("/api/categories/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRemovesCategory() throws Exception {
        ProductCategory category = saveCategory("Books");

        mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategoryWithProductsReturnsConflictAndKeepsTheProducts() throws Exception {
        ProductCategory category = saveCategory("Books");
        Product product = new Product("Clean Code", null, new BigDecimal("39.90"), 5);
        product.setCategory(category);
        entityManager.persist(product);
        entityManager.flush();

        mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(Product.class, product.getId())).isNotNull();
    }

    private ProductCategory saveCategory(String name) {
        ProductCategory category = new ProductCategory(name, null);
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }
}
