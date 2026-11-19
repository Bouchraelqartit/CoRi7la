package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByImmatriculationIgnoreCase(String immatriculation);

    boolean existsByImmatriculationIgnoreCaseAndIdNot(
            String immatriculation,
            Long id
    );

    List<Vehicule> findByConducteurIdOrderByIdAsc(Long conducteurId);
}