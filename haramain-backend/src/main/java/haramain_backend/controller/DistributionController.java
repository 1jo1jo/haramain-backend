package haramain_backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import haramain_backend.model.Distribution;
import haramain_backend.model.User;
import haramain_backend.repository.DistributionRepository;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/distributions")
public class DistributionController {

    private final DistributionRepository distributionRepository;
    private final UserRepository userRepository;

    public DistributionController(
            DistributionRepository distributionRepository,
            UserRepository userRepository) {

        this.distributionRepository = distributionRepository;
        this.userRepository = userRepository;
    }

    private User getDistributor(Principal principal) {

        if (principal == null) {
            throw new RuntimeException("يجب تسجيل الدخول أولاً");
        }

        int userId = Integer.parseInt(principal.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("المستخدم غير موجود"));

        if (!user.isActive()) {
            throw new RuntimeException("الحساب غير نشط");
        }

        if (!"courier".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("هذا الحساب ليس حساب مندوب");
        }

        return user;
    }
    // عرض جميع التوزيعات
    @GetMapping
    public List<Distribution> getAllDistributions() {
        return distributionRepository.findAll();
    }

    // عرض التوزيعات المتاحة للاستلام
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDistributions(
            Principal principal) {

        try {
            getDistributor(principal);

            return ResponseEntity.ok(
                    distributionRepository
                            .findByDistributorIDIsNullAndDistributionStatusNotIgnoreCase("Completed")
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // عرض المهام الخاصة بالمندوب الحالي
    @GetMapping("/my")
    public ResponseEntity<?> getMyDistributions(
            Principal principal) {

        try {
            User distributor = getDistributor(principal);

            return ResponseEntity.ok(
                    distributionRepository.findByDistributorID(
                            distributor.getUserId()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // استلام مهمة توزيع
    @PutMapping("/{id}/claim")
    public ResponseEntity<?> claimDistribution(
            @PathVariable Integer id,
            Principal principal) {

        try {
            User distributor = getDistributor(principal);

            Distribution distribution = distributionRepository
                    .findById(id)
                    .orElse(null);

            if (distribution == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "التوزيع غير موجود"));
            }

            if (distribution.getDistributorID() != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "تم استلام هذه المهمة مسبقاً"));
            }

            distribution.setDistributorID(distributor.getUserId());
            distribution.setDistributionStatus("ASSIGNED");

            distributionRepository.save(distribution);

            return ResponseEntity.ok(distribution);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // تحديث حالة التوزيع
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDistributionStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request,
            Principal principal) {

        try {
            User distributor = getDistributor(principal);

            Distribution distribution = distributionRepository
                    .findById(id)
                    .orElse(null);

            if (distribution == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "التوزيع غير موجود"));
            }

            if (distribution.getDistributorID() == null
                    || distribution.getDistributorID()
                    != distributor.getUserId()) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "هذه المهمة ليست مسندة إليك"));
            }

            String status = request.get("distributionStatus");

            List<String> allowedStatuses = List.of(
                    "PICKED_UP",
                    "IN_TRANSIT",
                    "DELIVERED"
            );

            if (status == null || !allowedStatuses.contains(status)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "حالة التوزيع غير صحيحة"));
            }

            distribution.setDistributionStatus(status);

            distributionRepository.save(distribution);

            return ResponseEntity.ok(distribution);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // إنشاء عملية توزيع
    @PostMapping
    public Distribution createDistribution(
            @RequestBody Distribution distribution) {

        return distributionRepository.save(distribution);
    }
}
