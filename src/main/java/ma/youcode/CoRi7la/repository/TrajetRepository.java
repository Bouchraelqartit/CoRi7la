package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ma.youcode.CoRi7la.dto.TrajetRechercheResponse;
import ma.youcode.CoRi7la.entity.StatutReservation;
import ma.youcode.CoRi7la.entity.StatutTrajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);
    @EntityGraph(attributePaths = {
            "conducteur",
            "vehicule",
            "vehicule.conducteur",
            "pointsArret"
    })
    Optional<Trajet> findWithDetailsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trajet t WHERE t.id = :id")
    Optional<Trajet> findByIdForUpdate(@Param("id") Long id);

    @Query(
            value = """
                SELECT new ma.youcode.CoRi7la.dto.TrajetRechercheResponse(
                    t.id,
                    c.id,
                    c.nom,
                    c.prenom,
                    montee.ville,
                    descente.ville,
                    montee.heurePassage,
                    descente.heurePassage,
                    descente.prixDepuisDepart - montee.prixDepuisDepart,
                    t.placesProposees - COALESCE(
                        (SELECT SUM(r.nombrePlaces)
                         FROM Reservation r
                         WHERE r.trajet = t
                           AND r.statut = :statutReservation),
                        0L
                    )
                )
                FROM Trajet t
                JOIN t.conducteur c
                JOIN t.pointsArret montee
                JOIN t.pointsArret descente
                WHERE t.statut = :statutTrajet
                  AND LOWER(montee.ville) = LOWER(:villeMontee)
                  AND LOWER(descente.ville) = LOWER(:villeDescente)
                  AND montee.ordre < descente.ordre
                  AND t.dateDepart >= :debutJour
                  AND t.dateDepart < :finJour
                  AND t.dateDepart > :maintenant
                  AND t.placesProposees - COALESCE(
                      (SELECT SUM(r.nombrePlaces)
                       FROM Reservation r
                       WHERE r.trajet = t
                         AND r.statut = :statutReservation),
                      0L
                  ) >= :nombrePlaces
                  AND descente.prixDepuisDepart
                      - montee.prixDepuisDepart <= :prixMax
                ORDER BY t.dateDepart ASC, t.id ASC
                """,
            countQuery = """
                SELECT COUNT(t)
                FROM Trajet t
                JOIN t.pointsArret montee
                JOIN t.pointsArret descente
                WHERE t.statut = :statutTrajet
                  AND LOWER(montee.ville) = LOWER(:villeMontee)
                  AND LOWER(descente.ville) = LOWER(:villeDescente)
                  AND montee.ordre < descente.ordre
                  AND t.dateDepart >= :debutJour
                  AND t.dateDepart < :finJour
                  AND t.dateDepart > :maintenant
                  AND t.placesProposees - COALESCE(
                      (SELECT SUM(r.nombrePlaces)
                       FROM Reservation r
                       WHERE r.trajet = t
                         AND r.statut = :statutReservation),
                      0L
                  ) >= :nombrePlaces
                  AND descente.prixDepuisDepart
                      - montee.prixDepuisDepart <= :prixMax
                """
    )
    Page<TrajetRechercheResponse> rechercher(
            @Param("villeMontee") String villeMontee,
            @Param("villeDescente") String villeDescente,
            @Param("debutJour") LocalDateTime debutJour,
            @Param("finJour") LocalDateTime finJour,
            @Param("maintenant") LocalDateTime maintenant,
            @Param("nombrePlaces") int nombrePlaces,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statutTrajet") StatutTrajet statutTrajet,
            @Param("statutReservation") StatutReservation statutReservation,
            Pageable pageable
    );
}