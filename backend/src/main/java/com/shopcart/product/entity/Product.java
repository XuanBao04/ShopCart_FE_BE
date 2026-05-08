package com.shopcart.product.entity;

import com.shopcart.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
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
