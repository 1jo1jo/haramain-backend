package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Rating;

public interface RatingRepository extends JpaRepository<Rating, Integer> {
}