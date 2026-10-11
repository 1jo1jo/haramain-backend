package haramain_backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{id}")
    public Organisation getOrganisationById(@PathVariable int id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organisation not found"));
    }

}
