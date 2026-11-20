package ma.youcode.CoRi7la.dto;

import ma.youcode.CoRi7la.entity.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long passagerId,
        Long trajetId,
        PointArretResponse pointMontee,
        PointArretResponse pointDescente,
        int nombrePlaces,
        BigDecimal montantTotal,
        LocalDateTime dateReservation,
        StatutReservation statut
) {
}