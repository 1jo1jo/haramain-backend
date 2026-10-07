package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Distribution;

public interface DistributionRepository extends JpaRepository<Distribution, Integer> {
}