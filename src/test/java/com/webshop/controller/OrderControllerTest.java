package com.webshop.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.webshop.entity.Customer;
import com.webshop.entity.CustomerAddress;
import com.webshop.entity.Product;
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
class OrderControllerTest {

    private static final int UNKNOWN_ID = 999_999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createReturnsOrderCombiningCustomerAddressAndProducts() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        CustomerAddress address = saveAddress(customer, "Main Street 1");
        Product book = saveProduct("Clean Code", "39.90", 10);
        Product toy = saveProduct("Teddy Bear", "15.00", 10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d, "shippingAddressId": %d,
                                 "items": [{"productId": %d, "quantity": 2}, {"productId": %d, "quantity": 1}]}
                                """.formatted(customer.getId(), address.getId(), book.getId(), toy.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderDate").exists())
                .andExpect(jsonPath("$.deliveryDate").doesNotExist())
                .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.customer.email").value("ada@example.com"))
                .andExpect(jsonPath("$.shippingAddress.streetAddress").value("Main Street 1"))
                .andExpect(jsonPath("$.items[*].productName").value(containsInAnyOrder("Clean Code", "Teddy Bear")))
                .andExpect(jsonPath("$.items[?(@.productName == 'Clean Code')].quantity").value(contains(2)))
                .andExpect(jsonPath("$.items[?(@.productName == 'Clean Code')].unitPrice").value(contains(39.90)))
                .andExpect(jsonPath("$.items[?(@.productName == 'Clean Code')].lineTotal").value(contains(79.80)))
                .andExpect(jsonPath("$.totalAmount").value(94.80));
    }

    @Test
    void createReducesTheStockOfTheOrderedProducts() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        Product book = saveProduct("Clean Code", "39.90", 10);

        placeOrder(customer, book, 3);

        assertThat(stockOf(book)).isEqualTo(7);
    }

    @Test
    void createKeepsTheUnitPriceWhenTheProductPriceChangesLater() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        Product book = saveProduct("Clean Code", "39.90", 10);
        int orderId = placeOrder(customer, book, 1);

        entityManager.find(Product.class, book.getId()).setPrice(new BigDecimal("99.00"));
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(jsonPath("$.items[0].unitPrice").value(39.90))
                .andExpect(jsonPath("$.totalAmount").value(39.90));
    }

    @Test
    void createWithoutShippingAddressIsAllowed() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        Product book = saveProduct("Clean Code", "39.90", 10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customer.getId(), book.getId(), 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").doesNotExist());
    }

    @Test
    void createWithMoreThanTheStockReturnsConflict() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        Product book = saveProduct("Clean Code", "39.90", 2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customer.getId(), book.getId(), 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("Clean Code")));
    }

    @Test
    void createWithoutItemsReturnsBadRequest() throws Exception {
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d, "items": []}
                                """.formatted(customer.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void createWithZeroQuantityReturnsBadRequestNamingTheItem() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        Product book = saveProduct("Clean Code", "39.90", 10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customer.getId(), book.getId(), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].quantity']").exists());
    }

    @Test
    void createWithTheSameProductTwiceReturnsBadRequest() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        Product book = saveProduct("Clean Code", "39.90", 10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d,
                                 "items": [{"productId": %d, "quantity": 1}, {"productId": %d, "quantity": 2}]}
                                """.formatted(customer.getId(), book.getId(), book.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithUnknownCustomerReturnsBadRequest() throws Exception {
        Product book = saveProduct("Clean Code", "39.90", 10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(UNKNOWN_ID, book.getId(), 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Customer")));
    }

    @Test
    void createWithUnknownProductReturnsBadRequest() throws Exception {
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customer.getId(), UNKNOWN_ID, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Product")));
    }

    @Test
    void createWithShippingAddressOfAnotherCustomerReturnsBadRequest() throws Exception {
        Customer ada = saveCustomer("ada@example.com");
        CustomerAddress addressOfGrace = saveAddress(saveCustomer("grace@example.com"), "Other Street 9");
        Product book = saveProduct("Clean Code", "39.90", 10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d, "shippingAddressId": %d, "items": [{"productId": %d, "quantity": 1}]}
                                """.formatted(ada.getId(), addressOfGrace.getId(), book.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("address")));
    }

    @Test
    void getByIdReturnsOrder() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        int orderId = placeOrder(customer, saveProduct("Clean Code", "39.90", 10), 2);

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.items[0].productName").value("Clean Code"))
                .andExpect(jsonPath("$.totalAmount").value(79.80));
    }

    @Test
    void getByUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsAllOrders() throws Exception {
        Product book = saveProduct("Clean Code", "39.90", 10);
        int orderOfAda = placeOrder(saveCustomer("ada@example.com"), book, 1);
        int orderOfGrace = placeOrder(saveCustomer("grace@example.com"), book, 1);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(containsInAnyOrder(orderOfAda, orderOfGrace)));
    }

    @Test
    void listFiltersByCustomer() throws Exception {
        Product book = saveProduct("Clean Code", "39.90", 10);
        Customer ada = saveCustomer("ada@example.com");
        int orderOfAda = placeOrder(ada, book, 1);
        placeOrder(saveCustomer("grace@example.com"), book, 1);

        mockMvc.perform(get("/api/orders").param("customerId", ada.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(orderOfAda)));
    }

    @Test
    void statusCanMoveFromPendingToConfirmedToShipped() throws Exception {
        int orderId = placeOrder(saveCustomer("ada@example.com"), saveProduct("Clean Code", "39.90", 10), 1);

        changeStatus(orderId, "CONFIRMED").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        changeStatus(orderId, "SHIPPED").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void deliveringAnOrderSetsTheDeliveryDate() throws Exception {
        int orderId = placeOrder(saveCustomer("ada@example.com"), saveProduct("Clean Code", "39.90", 10), 1);
        changeStatus(orderId, "CONFIRMED");
        changeStatus(orderId, "SHIPPED");

        changeStatus(orderId, "DELIVERED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveryDate").exists());
    }

    @Test
    void statusCannotSkipAStep() throws Exception {
        int orderId = placeOrder(saveCustomer("ada@example.com"), saveProduct("Clean Code", "39.90", 10), 1);

        changeStatus(orderId, "SHIPPED").andExpect(status().isConflict());
    }

    @Test
    void cancellingAnOrderPutsTheStockBack() throws Exception {
        Product book = saveProduct("Clean Code", "39.90", 10);
        int orderId = placeOrder(saveCustomer("ada@example.com"), book, 4);

        changeStatus(orderId, "CANCELLED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(stockOf(book)).isEqualTo(10);
    }

    @Test
    void shippedOrderCannotBeCancelled() throws Exception {
        int orderId = placeOrder(saveCustomer("ada@example.com"), saveProduct("Clean Code", "39.90", 10), 1);
        changeStatus(orderId, "CONFIRMED");
        changeStatus(orderId, "SHIPPED");

        changeStatus(orderId, "CANCELLED").andExpect(status().isConflict());
    }

    @Test
    void cancelledOrderCannotChangeStatusAgain() throws Exception {
        int orderId = placeOrder(saveCustomer("ada@example.com"), saveProduct("Clean Code", "39.90", 10), 1);
        changeStatus(orderId, "CANCELLED");

        changeStatus(orderId, "CONFIRMED").andExpect(status().isConflict());
    }

    @Test
    void unknownStatusValueReturnsBadRequest() throws Exception {
        int orderId = placeOrder(saveCustomer("ada@example.com"), saveProduct("Clean Code", "39.90", 10), 1);

        changeStatus(orderId, "LOST").andExpect(status().isBadRequest());
    }

    @Test
    void changingStatusOfUnknownOrderReturnsNotFound() throws Exception {
        changeStatus(UNKNOWN_ID, "CONFIRMED").andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions changeStatus(int orderId, String status) throws Exception {
        return mockMvc.perform(put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"" + status + "\"}"));
    }

    private int placeOrder(Customer customer, Product product, int quantity) throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customer.getId(), product.getId(), quantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        entityManager.flush();
        entityManager.clear();
        return JsonPath.read(response, "$.id");
    }

    private String orderJson(int customerId, int productId, int quantity) {
        return """
                {"customerId": %d, "items": [{"productId": %d, "quantity": %d}]}
                """.formatted(customerId, productId, quantity);
    }

    private int stockOf(Product product) {
        entityManager.flush();
        entityManager.clear();
        return entityManager.find(Product.class, product.getId()).getStockQuantity();
    }

    // Saved rows are flushed and the persistence context is cleared, so each request reads from the database
    private Customer saveCustomer(String email) {
        Customer customer = new Customer("Ada", "Lovelace", email, null);
        entityManager.persist(customer);
        entityManager.flush();
        entityManager.clear();
        return customer;
    }

    private CustomerAddress saveAddress(Customer customer, String streetAddress) {
        CustomerAddress address = new CustomerAddress(streetAddress, "00100", "Helsinki", "Finland");
        address.setCustomer(entityManager.getReference(Customer.class, customer.getId()));
        entityManager.persist(address);
        entityManager.flush();
        entityManager.clear();
        return address;
    }

    private Product saveProduct(String name, String price, int stockQuantity) {
        Product product = new Product(name, null, new BigDecimal(price), stockQuantity);
        entityManager.persist(product);
        entityManager.flush();
        entityManager.clear();
        return product;
    }
}
