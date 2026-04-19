package com.shopcart.entity;

import com.shopcart.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product{
    @Id
    @Column(name = "product_id",length = 50)
    private String id;
    
    @Column(nullable= false)
    private String name;
    
    @Column(nullable = false)
    private Long price;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
}
