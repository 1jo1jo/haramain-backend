package haramain_backend.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PostMapping; import org.springframework.web.bind.annotation.RequestBody; import org.springframework.web.bind.annotation.RestController;
import haramain_backend.model.Donation; import haramain_backend.repository.DonationRepository;
@RestController public class DonationController {
    private final DonationRepository donationRepository;

    public DonationController(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @GetMapping("/donations")
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    @PostMapping("/donations")
    public Donation createDonation(@RequestBody Donation donation) {
        return donationRepository.save(donation);
    }
}