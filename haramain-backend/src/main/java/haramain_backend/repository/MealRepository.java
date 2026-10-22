 package haramain_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Meal;

public interface MealRepository extends JpaRepository<Meal, Integer> {

    List<Meal> findByOrganisationID(Integer organisationID);
}
