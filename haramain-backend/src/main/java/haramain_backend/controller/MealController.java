package haramain_backend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import haramain_backend.model.Meal;
import haramain_backend.model.User;
import haramain_backend.repository.MealRepository;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealRepository mealRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public MealController(
            MealRepository mealRepository,
            UserRepository userRepository,
            Cloudinary cloudinary) {

        this.mealRepository = mealRepository;
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    // عرض جميع الوجبات
    @GetMapping
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    // عرض وجبة واحدة
    @GetMapping("/{id}")
    public Meal getMealById(@PathVariable int id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "الوجبة غير موجودة"));
    }

    // عرض وجبات منظمة المطعم المسجل دخوله
    @GetMapping("/my-meals")
    public ResponseEntity<?> getMyMeals(
            Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        return ResponseEntity.ok(
                mealRepository.findByOrganisationID(
                        restaurant.getOrganisationId()));
    }

    // إضافة وجبة جديدة
    @PostMapping
    public ResponseEntity<?> createMeal(
            @RequestBody Meal meal,
            Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        validateMeal(meal);

        if (meal.getImage() == null
                || meal.getImage().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "يرجى رفع صورة الوجبة أولًا"));
        }

        // تحديد المنظمة من الحساب الموثق، وليس من بيانات العميل
        meal.setOrganisationID(
                restaurant.getOrganisationId());

        // قاعدة البيانات تولد MealID تلقائيًا
        meal.setMealID(0);

        Meal savedMeal = mealRepository.save(meal);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "تمت إضافة الوجبة بنجاح",
                        "meal", savedMeal));
    }

    // تعديل وجبة تابعة لمنظمة المطعم فقط
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeal(
            @PathVariable int id,
            @RequestBody Meal updatedMeal,
            Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        Meal existingMeal = getOwnedMeal(
                id, restaurant.getOrganisationId());

        validateMeal(updatedMeal);

        if (updatedMeal.getImage() == null
                || updatedMeal.getImage().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "رابط صورة الوجبة مطلوب"));
        }

        existingMeal.setMealName(updatedMeal.getMealName().trim());
        existingMeal.setDescription(updatedMeal.getDescription().trim());
        existingMeal.setPrice(updatedMeal.getPrice());
        existingMeal.setQuantity(updatedMeal.getQuantity());
        existingMeal.setImage(updatedMeal.getImage().trim());

        Meal savedMeal = mealRepository.save(existingMeal);

        return ResponseEntity.ok(
                Map.of(
                        "message", "تم تعديل الوجبة بنجاح",
                        "meal", savedMeal));
    }

    // رفع صورة الوجبة إلى Cloudinary
    @PostMapping(
            value = "/upload-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMealImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        getAuthenticatedRestaurant(authentication);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "يرجى اختيار صورة أولًا"));
        }

        // الحد الأقصى 5 ميجابايت
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "حجم الصورة يجب ألا يتجاوز 5 ميجابايت"));
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        ).contains(contentType.toLowerCase(Locale.ROOT))) {

            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "الصيغ المسموحة: JPG وPNG وWEBP"));
        }

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "haramain-meals");
            options.put("resource_type", "image");

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    options);

            Object secureUrl = result.get("secure_url");

            if (secureUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "لم يتم استلام رابط الصورة");
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "تم رفع الصورة بنجاح",
                            "imageUrl", secureUrl.toString()));

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "تعذر رفع الصورة إلى خدمة التخزين",
                    exception);
        }
    }

    // حذف وجبة تابعة لمنظمة المطعم فقط
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeal(
            @PathVariable int id,
            Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        Meal meal = getOwnedMeal(
                id, restaurant.getOrganisationId());

        mealRepository.delete(meal);

        return ResponseEntity.ok(
                Map.of("message", "تم حذف الوجبة بنجاح"));
    }

    // التحقق من حساب المطعم
    private User getAuthenticatedRestaurant(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "يرجى تسجيل الدخول");
        }

        int userId;

        try {
            userId = Integer.parseInt(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "تعذر تحديد المستخدم الحالي");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "حساب المستخدم غير موجود"));

        if (!user.isActive()
                || user.getRole() == null
                || !user.getRole().equalsIgnoreCase("restaurant")
                || user.getOrganisationId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "هذه العملية متاحة لحساب المطعم المرتبط بمنظمة فقط");
        }

        return user;
    }

    // التأكد من أن الوجبة تابعة لمنظمة المطعم الحالي
    private Meal getOwnedMeal(int mealId, int organisationId) {

        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "الوجبة غير موجودة"));

        if (meal.getOrganisationID() != organisationId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "لا يمكنك إدارة وجبات منظمة أخرى");
        }

        return meal;
    }

    // التحقق من البيانات
    private void validateMeal(Meal meal) {

        if (meal == null
                || meal.getMealName() == null
                || meal.getMealName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "اسم الوجبة مطلوب");
        }

        if (meal.getMealName().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "اسم الوجبة يجب ألا يتجاوز 100 حرف");
        }

        if (meal.getDescription() == null
                || meal.getDescription().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "وصف الوجبة مطلوب");
        }

        if (!Double.isFinite(meal.getPrice())
                || meal.getPrice() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "سعر الوجبة يجب أن يكون أكبر من صفر");
        }

        if (meal.getQuantity() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "الكمية لا يمكن أن تكون سالبة");
        }
    }
}
