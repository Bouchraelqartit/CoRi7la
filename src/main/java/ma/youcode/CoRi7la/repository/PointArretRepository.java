package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.PointArret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointArretRepository
        extends JpaRepository<PointArret, Long> {

    Optional<PointArret> findByIdAndTrajetId(
            Long id,
            Long trajetId
    );
}