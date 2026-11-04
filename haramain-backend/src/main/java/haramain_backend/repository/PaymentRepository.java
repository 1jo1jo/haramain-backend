package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Payment;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByRequestId(int requestId);

}