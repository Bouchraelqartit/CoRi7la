package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    Optional<Conducteur> findByEmailIgnoreCase(String email);

    List<Conducteur> findByNomContainingIgnoreCase(String nom);
}