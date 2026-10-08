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

import com.webshop.entity.Customer;
import com.webshop.entity.CustomerAddress;
import com.webshop.entity.Order;
import jakarta.persistence.EntityManager;
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
class CustomerControllerTest {

    private static final int UNKNOWN_ID = 999_999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createReturnsCreatedCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Ada", "Lovelace", "ada@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.phone").value("+358 40 1234567"))
                .andExpect(jsonPath("$.addresses").isEmpty());
    }

    @Test
    void createWithInvalidFieldsReturnsBadRequestNamingEachField() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": " ", "lastName": "", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createWithEmailOfAnotherCustomerReturnsConflict() throws Exception {
        saveCustomer("ada@example.com");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Augusta", "King", "ADA@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void getByIdReturnsCustomerWithAddresses() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        saveAddress(customer, "Main Street 1");

        mockMvc.perform(get("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.addresses[*].streetAddress").value(contains("Main Street 1")));
    }

    @Test
    void getByUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsAllCustomersWithTheirAddresses() throws Exception {
        Customer ada = saveCustomer("ada@example.com");
        saveAddress(ada, "Main Street 1");
        saveAddress(ada, "Side Street 2");
        saveCustomer("grace@example.com");

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(containsInAnyOrder("ada@example.com", "grace@example.com")))
                .andExpect(jsonPath("$[?(@.email == 'ada@example.com')].addresses[*].streetAddress")
                        .value(containsInAnyOrder("Main Street 1", "Side Street 2")));
    }

    @Test
    void updateReplacesCustomerFields() throws Exception {
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Augusta", "King", "augusta@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.firstName").value("Augusta"))
                .andExpect(jsonPath("$.email").value("augusta@example.com"));
    }

    @Test
    void updateKeepingTheSameEmailIsAllowed() throws Exception {
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Augusta", "King", "ada@example.com")))
                .andExpect(status().isOk());
    }

    @Test
    void updateToEmailOfAnotherCustomerReturnsConflict() throws Exception {
        saveCustomer("grace@example.com");
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Ada", "Lovelace", "grace@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("Ada", "Lovelace", "ada@example.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesCustomerAndTheirAddresses() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        CustomerAddress address = saveAddress(customer, "Main Street 1");

        mockMvc.perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Customer.class, customer.getId())).isNull();
        assertThat(entityManager.find(CustomerAddress.class, address.getId())).isNull();
    }

    @Test
    void deleteCustomerWithOrdersReturnsConflictAndKeepsTheCustomer() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        saveOrder(customer, null);

        mockMvc.perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(Customer.class, customer.getId())).isNotNull();
    }

    @Test
    void createAddressReturnsCreatedAddress() throws Exception {
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(post("/api/customers/{customerId}/addresses", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("Main Street 1", "Helsinki")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.streetAddress").value("Main Street 1"))
                .andExpect(jsonPath("$.postalCode").value("00100"))
                .andExpect(jsonPath("$.city").value("Helsinki"))
                .andExpect(jsonPath("$.country").value("Finland"));
    }

    @Test
    void createAddressForUnknownCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/customers/{customerId}/addresses", UNKNOWN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("Main Street 1", "Helsinki")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAddressWithoutStreetAndCityReturnsBadRequest() throws Exception {
        Customer customer = saveCustomer("ada@example.com");

        mockMvc.perform(post("/api/customers/{customerId}/addresses", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postalCode": "00100"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.streetAddress").exists())
                .andExpect(jsonPath("$.errors.city").exists());
    }

    @Test
    void listAddressesReturnsOnlyTheAddressesOfThatCustomer() throws Exception {
        Customer ada = saveCustomer("ada@example.com");
        saveAddress(ada, "Main Street 1");
        saveAddress(saveCustomer("grace@example.com"), "Other Street 9");

        mockMvc.perform(get("/api/customers/{customerId}/addresses", ada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].streetAddress").value(contains("Main Street 1")));
    }

    @Test
    void listAddressesOfUnknownCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{customerId}/addresses", UNKNOWN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddressReturnsAddress() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        CustomerAddress address = saveAddress(customer, "Main Street 1");

        mockMvc.perform(get("/api/customers/{customerId}/addresses/{addressId}", customer.getId(), address.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(address.getId()))
                .andExpect(jsonPath("$.streetAddress").value("Main Street 1"));
    }

    @Test
    void getAddressOfAnotherCustomerReturnsNotFound() throws Exception {
        Customer ada = saveCustomer("ada@example.com");
        CustomerAddress addressOfGrace = saveAddress(saveCustomer("grace@example.com"), "Other Street 9");

        mockMvc.perform(get("/api/customers/{customerId}/addresses/{addressId}", ada.getId(), addressOfGrace.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAddressReplacesFields() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        CustomerAddress address = saveAddress(customer, "Main Street 1");

        mockMvc.perform(put("/api/customers/{customerId}/addresses/{addressId}", customer.getId(), address.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("New Road 5", "Espoo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(address.getId()))
                .andExpect(jsonPath("$.streetAddress").value("New Road 5"))
                .andExpect(jsonPath("$.city").value("Espoo"));
    }

    @Test
    void updateAddressOfAnotherCustomerReturnsNotFound() throws Exception {
        Customer ada = saveCustomer("ada@example.com");
        CustomerAddress addressOfGrace = saveAddress(saveCustomer("grace@example.com"), "Other Street 9");

        mockMvc.perform(put("/api/customers/{customerId}/addresses/{addressId}", ada.getId(), addressOfGrace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressJson("New Road 5", "Espoo")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAddressRemovesAddress() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        CustomerAddress address = saveAddress(customer, "Main Street 1");

        mockMvc.perform(delete("/api/customers/{customerId}/addresses/{addressId}", customer.getId(), address.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{customerId}/addresses/{addressId}", customer.getId(), address.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAddressUsedAsShippingAddressReturnsConflictAndKeepsTheAddress() throws Exception {
        Customer customer = saveCustomer("ada@example.com");
        CustomerAddress address = saveAddress(customer, "Main Street 1");
        saveOrder(customer, address);

        mockMvc.perform(delete("/api/customers/{customerId}/addresses/{addressId}", customer.getId(), address.getId()))
                .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(CustomerAddress.class, address.getId())).isNotNull();
    }

    private String customerJson(String firstName, String lastName, String email) {
        return """
                {"firstName": "%s", "lastName": "%s", "email": "%s", "phone": "+358 40 1234567"}
                """.formatted(firstName, lastName, email);
    }

    private String addressJson(String streetAddress, String city) {
        return """
                {"streetAddress": "%s", "postalCode": "00100", "city": "%s", "country": "Finland"}
                """.formatted(streetAddress, city);
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
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(entityManager.getReference(Customer.class, customer.getId()));
        address.setStreetAddress(streetAddress);
        address.setCity("Helsinki");
        entityManager.persist(address);
        entityManager.flush();
        entityManager.clear();
        return address;
    }

    private void saveOrder(Customer customer, CustomerAddress shippingAddress) {
        Order order = new Order();
        order.setCustomer(entityManager.getReference(Customer.class, customer.getId()));
        if (shippingAddress != null) {
            order.setShippingAddress(entityManager.getReference(CustomerAddress.class, shippingAddress.getId()));
        }
        order.setOrderDate(LocalDateTime.now());
        entityManager.persist(order);
        entityManager.flush();
        entityManager.clear();
    }
}
