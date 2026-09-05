package com.example.coresto.repository;

import com.example.coresto.domain.ordering.Order;
import com.example.coresto.domain.common.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBranchIdAndStatusIn(UUID branchId, Collection<OrderStatus> statuses);
}
