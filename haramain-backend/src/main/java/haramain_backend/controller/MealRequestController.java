package haramain_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import haramain_backend.model.MealRequest;
import haramain_backend.repository.MealRequestRepository;

@RestController
public class MealRequestController {

    private final MealRequestRepository mealRequestRepository;

    public MealRequestController(
            MealRequestRepository mealRequestRepository) {
        this.mealRequestRepository = mealRequestRepository;
    }

    // جلب جميع الطلبات
    @GetMapping("/meal-requests")
    public List<MealRequest> getAllMealRequests() {
        return mealRequestRepository.findAll();
    }

    // جلب طلبات مستخدم معين
    @GetMapping("/meal-requests/user/{userId}")
    public List<MealRequest> getMealRequestsByUser(
            @PathVariable int userId) {
        return mealRequestRepository.findByUserId(userId);
    }

    // إنشاء طلب جديد
    @PostMapping("/meal-requests")
    public MealRequest createMealRequest(
            @RequestBody MealRequest mealRequest) {
        return mealRequestRepository.save(mealRequest);
    }

    // تحديث حالة الطلب
    @PutMapping("/meal-requests/{id}/status")
    public MealRequest updateMealRequestStatus(
            @PathVariable int id,
            @RequestBody MealRequest updatedRequest) {

        MealRequest mealRequest = mealRequestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Meal request not found"));

        mealRequest.setStatus(updatedRequest.getStatus());

        return mealRequestRepository.save(mealRequest);
    }
}