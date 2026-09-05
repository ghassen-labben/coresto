package com.example.coresto.repository;

import com.example.coresto.domain.ordering.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
