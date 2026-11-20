package ma.youcode.CoRi7la.dto;

import ma.youcode.CoRi7la.entity.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        ConducteurResponse conducteur,
        VehiculeResponse vehicule,
        LocalDateTime dateDepart,
        int placesProposees,
        int placesRestantes,
        StatutTrajet statut,
        List<PointArretResponse> pointsArret
) {
}