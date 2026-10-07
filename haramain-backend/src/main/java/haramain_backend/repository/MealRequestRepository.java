package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.MealRequest;

public interface MealRequestRepository extends JpaRepository<MealRequest, Integer> {
}