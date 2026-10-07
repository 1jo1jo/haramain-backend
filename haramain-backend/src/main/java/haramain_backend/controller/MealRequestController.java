package haramain_backend.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PostMapping; import org.springframework.web.bind.annotation.RequestBody; import org.springframework.web.bind.annotation.RestController;
import haramain_backend.model.MealRequest; import haramain_backend.repository.MealRequestRepository;
@RestController public class MealRequestController {
    private final MealRequestRepository mealRequestRepository;

    public MealRequestController(MealRequestRepository mealRequestRepository) {
        this.mealRequestRepository = mealRequestRepository;
    }

    @GetMapping("/meal-requests")
    public List<MealRequest> getAllMealRequests() {
        return mealRequestRepository.findAll();
    }

    @PostMapping("/meal-requests")
    public MealRequest createMealRequest(@RequestBody MealRequest mealRequest) {
        return mealRequestRepository.save(mealRequest);
    }
}