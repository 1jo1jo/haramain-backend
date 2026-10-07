package haramain_backend.controller;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PostMapping; import org.springframework.web.bind.annotation.RequestBody; import org.springframework.web.bind.annotation.RestController;
import haramain_backend.model.RequestDetail; import haramain_backend.repository.RequestDetailRepository;
@RestController public class RequestDetailController {
    private final RequestDetailRepository requestDetailRepository;

    public RequestDetailController(RequestDetailRepository requestDetailRepository) {
        this.requestDetailRepository = requestDetailRepository;
    }

    @GetMapping("/request-details")
    public List<RequestDetail> getAllRequestDetails() {
        return requestDetailRepository.findAll();
    }

    @PostMapping("/request-details")
    public RequestDetail createRequestDetail(@RequestBody RequestDetail requestDetail) {
        return requestDetailRepository.save(requestDetail);
    }
}