package com.shopcart.entity;

import com.shopcart.entity.enums.ProductStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product{
    @Id
    @Column(name = "product_id",length = 50)
    public String id;
    
    @Column(nullable= false)
    public String name;
    
    @Column(nullable = false)
    public Long price;
    
    @Column(columnDefinition = "TEXT")
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProductStatus status;
    
    public Product() {}
    
    public Product(String id, String name, Long price, String description, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.status = status;
    }
}
