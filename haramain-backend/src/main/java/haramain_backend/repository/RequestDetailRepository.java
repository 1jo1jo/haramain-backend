package haramain_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import haramain_backend.model.RequestDetail;

public interface RequestDetailRepository
        extends JpaRepository<RequestDetail, Integer> {

    List<RequestDetail> findByRequestId(int requestId);
}
