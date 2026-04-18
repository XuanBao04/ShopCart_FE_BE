package com.shopcart.repository;

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
    
    // Bạn có thể định nghĩa thêm các hàm generic tại đây trong tương lai.
    // Ví dụ:
    // Optional<T> findByIdAndIsDeletedFalse(ID id);
    // void softDelete(ID id);
}