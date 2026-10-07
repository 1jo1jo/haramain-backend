package haramain_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import haramain_backend.model.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {

}