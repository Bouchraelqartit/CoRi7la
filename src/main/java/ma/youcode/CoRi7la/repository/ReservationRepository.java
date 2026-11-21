package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Reservation;
import ma.youcode.CoRi7la.entity.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT COALESCE(SUM(r.nombrePlaces), 0)
            FROM Reservation r
            WHERE r.trajet.id = :trajetId
              AND r.statut = :statut
            """)
    long compterPlacesReservees(
            @Param("trajetId") Long trajetId,
            @Param("statut") StatutReservation statut
    );

    boolean existsByPassagerIdAndTrajetIdAndStatut(
            Long passagerId,
            Long trajetId,
            StatutReservation statut
    );
}