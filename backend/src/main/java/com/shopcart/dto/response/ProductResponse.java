package com.shopcart.dto.response;

public class ProductResponse {
    public String id;
    public String name;
    public String description;
    public Long price;
    public String status;
    
    public ProductResponse() {}
    
    public ProductResponse(String id, String name, String description, Long price, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }
}
