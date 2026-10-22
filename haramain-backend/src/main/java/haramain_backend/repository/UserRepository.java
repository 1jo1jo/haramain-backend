package haramain_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import haramain_backend.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

}

