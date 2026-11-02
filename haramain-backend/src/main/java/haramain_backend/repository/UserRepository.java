package haramain_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import haramain_backend.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {


    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.role) = 'courier' AND u.isActive = true")
    List<User> findActiveCouriers();


}
