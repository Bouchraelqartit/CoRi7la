package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);
    @EntityGraph(attributePaths = {
            "conducteur",
            "vehicule",
            "vehicule.conducteur",
            "pointsArret"
    })
    Optional<Trajet> findWithDetailsById(Long id);
}