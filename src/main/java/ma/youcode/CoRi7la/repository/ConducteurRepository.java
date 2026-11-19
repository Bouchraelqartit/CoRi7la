package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    Optional<Conducteur> findByEmailIgnoreCase(String email);

    List<Conducteur> findByNomContainingIgnoreCase(String nom);

    @Query("""
        SELECT c FROM Conducteur c
        WHERE (:nom = ''
               OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
          AND (:email = '' OR LOWER(c.email) = LOWER(:email))
        ORDER BY c.id ASC
        """)
    List<Conducteur> rechercher(
            @Param("nom") String nom,
            @Param("email") String email
    );
}