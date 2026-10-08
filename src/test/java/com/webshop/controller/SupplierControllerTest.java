package com.webshop.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.webshop.entity.Product;
import com.webshop.entity.Supplier;
import com.webshop.entity.SupplierAddress;
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
class SupplierControllerTest {

    private static final int UNKNOWN_ID = 999_999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createReturnsCreatedSupplier() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("Book Wholesale")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Book Wholesale"))
                .andExpect(jsonPath("$.contactName").value("Grace Hopper"))
                .andExpect(jsonPath("$.phone").value("+358 9 1234567"))
                .andExpect(jsonPath("$.email").value("sales@example.com"))
                .andExpect(jsonPath("$.addresses").isEmpty());
    }

    @Test
    void createWithOnlyANameIsAllowed() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Book Wholesale"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithInvalidFieldsReturnsBadRequestNamingEachField() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void getByIdReturnsSupplierWithAddresses() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");
        saveAddress(supplier, "Warehouse Road 3");

        mockMvc.perform(get("/api/suppliers/{id}", supplier.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplier.getId()))
                .andExpect(jsonPath("$.addresses[*].streetAddress").value(contains("Warehouse Road 3")));
    }

    @Test
    void getByUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/suppliers/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsAllSuppliersWithTheirAddresses() throws Exception {
        Supplier books = saveSupplier("Book Wholesale");
        saveAddress(books, "Warehouse Road 3");
        saveAddress(books, "Harbour Street 8");
        saveSupplier("Toy Factory");

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(containsInAnyOrder("Book Wholesale", "Toy Factory")))
                .andExpect(jsonPath("$[?(@.name == 'Book Wholesale')].addresses[*].streetAddress")
                        .value(containsInAnyOrder("Warehouse Road 3", "Harbour Street 8")));
    }

    @Test
    void updateReplacesSupplierFields() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");

        mockMvc.perform(put("/api/suppliers/{id}", supplier.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("Nordic Books")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplier.getId()))
                .andExpect(jsonPath("$.name").value("Nordic Books"))
                .andExpect(jsonPath("$.contactName").value("Grace Hopper"));
    }

    @Test
    void updateUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/suppliers/{id}", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierJson("Nordic Books")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesSupplierAndItsAddresses() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");
        SupplierAddress address = saveAddress(supplier, "Warehouse Road 3");

        mockMvc.perform(delete("/api/suppliers/{id}", supplier.getId()))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Supplier.class, supplier.getId())).isNull();
        assertThat(entityManager.find(SupplierAddress.class, address.getId())).isNull();
    }

    @Test
    void deleteSupplierWithProductsReturnsConflictAndKeepsTheSupplier() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");
        Product product = new Product("Clean Code", null, new BigDecimal("39.90"), 5);
        product.setSupplier(entityManager.getReference(Supplier.class, supplier.getId()));
        entityManager.persist(product);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/suppliers/{id}", supplier.getId()))
                .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(Supplier.class, supplier.getId())).isNotNull();
    }

    @Test
    void createAddressReturnsCreatedAddress() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");

        mockMvc.perform(post("/api/suppliers/{supplierId}/addresses", supplier.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("Warehouse Road 3", "Tampere")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.streetAddress").value("Warehouse Road 3"))
                .andExpect(jsonPath("$.postalCode").value("33100"))
                .andExpect(jsonPath("$.city").value("Tampere"))
                .andExpect(jsonPath("$.country").value("Finland"));
    }

    @Test
    void createAddressForUnknownSupplierReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/suppliers/{supplierId}/addresses", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("Warehouse Road 3", "Tampere")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAddressWithoutStreetAndCityReturnsBadRequest() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");

        mockMvc.perform(post("/api/suppliers/{supplierId}/addresses", supplier.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postalCode": "33100"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.streetAddress").exists())
                .andExpect(jsonPath("$.errors.city").exists());
    }

    @Test
    void listAddressesReturnsOnlyTheAddressesOfThatSupplier() throws Exception {
        Supplier books = saveSupplier("Book Wholesale");
        saveAddress(books, "Warehouse Road 3");
        saveAddress(saveSupplier("Toy Factory"), "Other Street 9");

        mockMvc.perform(get("/api/suppliers/{supplierId}/addresses", books.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].streetAddress").value(contains("Warehouse Road 3")));
    }

    @Test
    void listAddressesOfUnknownSupplierReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/suppliers/{supplierId}/addresses", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddressOfAnotherSupplierReturnsNotFound() throws Exception {
        Supplier books = saveSupplier("Book Wholesale");
        SupplierAddress addressOfToys = saveAddress(saveSupplier("Toy Factory"), "Other Street 9");

        mockMvc.perform(get("/api/suppliers/{supplierId}/addresses/{addressId}", books.getId(), addressOfToys.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddressReturnsAddress() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");
        SupplierAddress address = saveAddress(supplier, "Warehouse Road 3");

        mockMvc.perform(get("/api/suppliers/{supplierId}/addresses/{addressId}", supplier.getId(), address.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(address.getId()))
                .andExpect(jsonPath("$.streetAddress").value("Warehouse Road 3"));
    }

    @Test
    void updateAddressReplacesFields() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");
        SupplierAddress address = saveAddress(supplier, "Warehouse Road 3");

        mockMvc.perform(put("/api/suppliers/{supplierId}/addresses/{addressId}", supplier.getId(), address.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("New Road 5", "Turku")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(address.getId()))
                .andExpect(jsonPath("$.streetAddress").value("New Road 5"))
                .andExpect(jsonPath("$.city").value("Turku"));
    }

    @Test
    void deleteAddressRemovesAddress() throws Exception {
        Supplier supplier = saveSupplier("Book Wholesale");
        SupplierAddress address = saveAddress(supplier, "Warehouse Road 3");

        mockMvc.perform(delete("/api/suppliers/{supplierId}/addresses/{addressId}", supplier.getId(), address.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/suppliers/{supplierId}/addresses/{addressId}", supplier.getId(), address.getId()))
                .andExpect(status().isNotFound());
    }

    private String supplierJson(String name) {
        return """
                {"name": "%s", "contactName": "Grace Hopper", "phone": "+358 9 1234567", "email": "sales@example.com"}
                """.formatted(name);
    }

    private String addressJson(String streetAddress, String city) {
        return """
                {"streetAddress": "%s", "postalCode": "33100", "city": "%s", "country": "Finland"}
                """.formatted(streetAddress, city);
    }

    // Saved rows are flushed and the persistence context is cleared, so each request reads from the database
    private Supplier saveSupplier(String name) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        entityManager.persist(supplier);
        entityManager.flush();
        entityManager.clear();
        return supplier;
    }

    private SupplierAddress saveAddress(Supplier supplier, String streetAddress) {
        SupplierAddress address = new SupplierAddress();
        address.setSupplier(entityManager.getReference(Supplier.class, supplier.getId()));
        address.setStreetAddress(streetAddress);
        address.setCity("Tampere");
        entityManager.persist(address);
        entityManager.flush();
        entityManager.clear();
        return address;
    }
}
