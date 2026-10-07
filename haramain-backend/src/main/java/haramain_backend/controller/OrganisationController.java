package haramain_backend.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import haramain_backend.model.Organisation;
import haramain_backend.repository.OrganisationRepository;

@RestController
@RequestMapping("/organisations")
public class OrganisationController {
    private final OrganisationRepository organisationRepository;

    public OrganisationController(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    @GetMapping
    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }
}