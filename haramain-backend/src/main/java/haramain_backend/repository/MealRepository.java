package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Meal;

public interface MealRepository extends JpaRepository<Meal, Integer> {
}