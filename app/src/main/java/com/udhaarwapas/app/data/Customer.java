package com.udhaarwapas.app.data;

/** Ek customer (grahak) — sirf naam, phone aur kab add hua. Balance entries se banta hai. */
public class Customer {
    public long id;
    public String name;
    public String phone;
    public long createdAt;

    public Customer(long id, String name, String phone, long createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
    }
}
