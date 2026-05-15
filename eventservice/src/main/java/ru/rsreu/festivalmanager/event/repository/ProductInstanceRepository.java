package ru.rsreu.festivalmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.festivalmanager.event.entity.stand.ProductInstance;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductInstanceRepository extends JpaRepository<ProductInstance, Long> {
    @Query("SELECT pi FROM ProductInstance pi " +
            "WHERE pi.product.id = :productId " +
            "AND pi.quantity = :quantity " +
            "AND pi.price = :price " +
            "AND (pi.description = :description OR (pi.description IS NULL AND :description IS NULL))")
    Optional<ProductInstance> findIdenticalInstance(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity,
            @Param("price") BigDecimal price,
            @Param("description") String description
    );
}
