package haramain_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import haramain_backend.model.Meal;
import haramain_backend.dto.MealDetails;

public interface MealRepository extends JpaRepository<Meal, Integer> {

    List<Meal> findByOrganisationID(Integer organisationID);

    @Query(value = """
    SELECT
        m.MealID AS mealID,
        m.organisationID AS organisationID,
        m.MealName AS mealName,
        m.Description AS description,
        m.Price AS price,
        m.Quantity AS quantity,
        m.Image AS image,
        o.organisationName AS organisationName,
        o.city AS city
    FROM meal m
    JOIN organisation o
        ON m.organisationID = o.organisationID
    ORDER BY o.city, o.organisationName, m.MealID
    """, nativeQuery = true)
    List<MealDetails> findAllMealsWithOrganisation();


}
