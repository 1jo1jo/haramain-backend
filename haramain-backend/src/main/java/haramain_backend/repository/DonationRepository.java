package haramain_backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Donation;

public interface DonationRepository extends JpaRepository<Donation, Integer> {


    List<Donation> findByRequestId(int requestId);

}
