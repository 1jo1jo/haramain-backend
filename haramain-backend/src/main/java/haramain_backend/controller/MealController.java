package haramain_backend.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.RestController;
import haramain_backend.model.Meal; import haramain_backend.repository.MealRepository;
@RestController @RequestMapping("/meals") public class MealController {
    private final MealRepository mealRepository;

    public MealController(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    @GetMapping
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }
}