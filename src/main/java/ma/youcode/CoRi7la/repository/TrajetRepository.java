package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);
}