package com.webshop.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.webshop.entity.Customer;
import com.webshop.entity.Order;
import com.webshop.entity.OrderItem;
import com.webshop.entity.OrderItemId;
import com.webshop.entity.Product;
import com.webshop.entity.ProductCategory;
import com.webshop.entity.Supplier;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class ProductControllerTest {

    private static final int UNKNOWN_ID = 999_999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createReturnsProductWithCategoryAndSupplier() throws Exception {
        ProductCategory books = saveCategory("Books");
        Supplier supplier = saveSupplier("Book Wholesale");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Clean Code", "39.90", 5, books.getId(), supplier.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Clean Code"))
                .andExpect(jsonPath("$.price").value(39.90))
                .andExpect(jsonPath("$.stockQuantity").value(5))
                .andExpect(jsonPath("$.category.id").value(books.getId()))
                .andExpect(jsonPath("$.category.name").value("Books"))
                .andExpect(jsonPath("$.supplier.id").value(supplier.getId()))
                .andExpect(jsonPath("$.supplier.name").value("Book Wholesale"));
    }

    @Test
    void createWithoutCategoryAndSupplierIsAllowed() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Clean Code", "39.90", 5, null, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.supplier").doesNotExist());
    }

    @Test
    void createWithInvalidFieldsReturnsBadRequestNamingEachField() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "price": -1, "stockQuantity": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stockQuantity").exists());
    }

    @Test
    void createWithoutPriceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Clean Code", "stockQuantity": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void createWithUnknownCategoryReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Clean Code", "39.90", 5, UNKNOWN_ID, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Category")));
    }

    @Test
    void createWithUnknownSupplierReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Clean Code", "39.90", 5, null, UNKNOWN_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Supplier")));
    }

    @Test
    void getByIdReturnsProduct() throws Exception {
        Product product = saveProduct("Clean Code", "39.90", saveCategory("Books"));

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.category.name").value("Books"));
    }

    @Test
    void getByUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsAllProducts() throws Exception {
        saveProduct("Refactoring", "45.00", null);
        saveProduct("Clean Code", "39.90", null);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(containsInAnyOrder("Clean Code", "Refactoring")));
    }

    @Test
    void listFiltersByNameIgnoringCase() throws Exception {
        saveProduct("Clean Code", "39.90", null);
        saveProduct("Refactoring", "45.00", null);

        mockMvc.perform(get("/api/products").param("name", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Clean Code")));
    }

    @Test
    void updateReplacesFieldsAndReferences() throws Exception {
        Product product = saveProduct("Clean Code", "39.90", saveCategory("Books"));
        ProductCategory programming = saveCategory("Programming");

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Clean Code 2nd Edition", "49.90", 12, programming.getId(), null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Clean Code 2nd Edition"))
                .andExpect(jsonPath("$.price").value(49.90))
                .andExpect(jsonPath("$.stockQuantity").value(12))
                .andExpect(jsonPath("$.category.name").value("Programming"));
    }

    @Test
    void updateUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/products/{id}", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Clean Code", "39.90", 5, null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesProduct() throws Exception {
        Product product = saveProduct("Clean Code", "39.90", null);

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownIdReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", UNKNOWN_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProductThatWasOrderedReturnsConflictAndKeepsTheProduct() throws Exception {
        Product product = saveProduct("Clean Code", "39.90", null);
        saveOrderContaining(product);

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(Product.class, product.getId())).isNotNull();
    }

    private String productJson(String name, String price, int stockQuantity, Integer categoryId, Integer supplierId) {
        return """
                {"name": "%s", "description": "A product", "price": %s, "stockQuantity": %d, "categoryId": %s, "supplierId": %s}
                """.formatted(name, price, stockQuantity, categoryId, supplierId);
    }

    private ProductCategory saveCategory(String name) {
        ProductCategory category = new ProductCategory(name, null);
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }

    private Supplier saveSupplier(String name) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        entityManager.persist(supplier);
        entityManager.flush();
        return supplier;
    }

    private Product saveProduct(String name, String price, ProductCategory category) {
        Product product = new Product(name, null, new BigDecimal(price), 5);
        product.setCategory(category);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }

    private void saveOrderContaining(Product product) {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com", null);
        entityManager.persist(customer);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        entityManager.persist(order);

        OrderItem item = new OrderItem();
        item.setId(new OrderItemId());
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(product.getPrice());
        entityManager.persist(item);
        entityManager.flush();
    }
}
