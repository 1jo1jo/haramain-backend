package haramain_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.MealRequest;

public interface MealRequestRepository extends JpaRepository<MealRequest, Integer> {

    // جلب طلبات جهة معينة
    List<MealRequest> findByRestaurantId(Integer restaurantId);

    // جلب الطلبات المسندة إلى مندوب معين
    List<MealRequest> findByCourierId(Integer courierId);

}
