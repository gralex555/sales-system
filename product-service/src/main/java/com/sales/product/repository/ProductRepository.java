package com.sales.product.repository;

import com.sales.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("UPDATE Product p " +
            "SET p.quantityAvailable = p.quantityAvailable - :quantity, " +
            "    p.quantityReserved = p.quantityReserved + :quantity " +
            "WHERE p.id = :productId AND p.quantityAvailable >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
