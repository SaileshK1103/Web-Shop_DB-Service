package com.webshop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CustomerAddresses")
public class CustomerAddress extends Address {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public CustomerAddress() {}

    public CustomerAddress(String streetAddress,String postalCode, String city, String country) {
        super(streetAddress,postalCode,city,country);
    }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
