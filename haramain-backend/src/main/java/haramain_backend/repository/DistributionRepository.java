package haramain_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import haramain_backend.model.Distribution;

public interface DistributionRepository
        extends JpaRepository<Distribution, Integer> {

    // جلب التوزيعات المسندة لمندوب محدد
    List<Distribution> findByDistributorID(Integer distributorID);

    // جلب التوزيعات التي لم يتم إسنادها لأي مندوب
    List<Distribution> findByDistributorIDIsNull();

    List<Distribution> findByDistributorIDIsNullAndDistributionStatusNotIgnoreCase(
            String distributionStatus
    );
}