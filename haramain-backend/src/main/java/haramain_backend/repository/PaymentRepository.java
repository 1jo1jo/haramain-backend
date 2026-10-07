package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}