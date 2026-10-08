package com.webshop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SupplierAddresses")
public class SupplierAddress extends Address {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    public SupplierAddress() {}

    public SupplierAddress(String streetAddress,String postalCode, String city, String country) {
        super(streetAddress,postalCode,city,country);
    }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
}
