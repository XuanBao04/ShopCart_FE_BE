package com.shopcart.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Generic Base Repository cho toàn bộ hệ thống.
 * T: Entity class
 * ID: Kiểu dữ liệu của khóa chính (thường là Long hoặc String)
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    
   
}