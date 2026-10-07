package haramain_backend.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PostMapping; import org.springframework.web.bind.annotation.RequestBody; import org.springframework.web.bind.annotation.RestController;
import haramain_backend.model.Distribution; import haramain_backend.repository.DistributionRepository;
@RestController public class DistributionController {
    private final DistributionRepository distributionRepository;

    public DistributionController(DistributionRepository distributionRepository) {
        this.distributionRepository = distributionRepository;
    }

    @GetMapping("/distributions")
    public List<Distribution> getAllDistributions() {
        return distributionRepository.findAll();
    }

    @PostMapping("/distributions")
    public Distribution createDistribution(@RequestBody Distribution distribution) {
        return distributionRepository.save(distribution);
    }
}