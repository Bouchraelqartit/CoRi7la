package ma.youcode.CoRi7la.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record TrajetPublicationRequest(

        @NotNull(message = "Le véhicule est obligatoire.")
        @Positive(message = "L'identifiant du véhicule doit être positif.")
        Long vehiculeId,

        @NotNull(message = "Le nombre de places est obligatoire.")
        @Min(value = 1, message = "Il faut proposer au moins une place.")
        @Max(value = 8, message = "Il est impossible de proposer plus de 8 places.")
        Integer placesProposees,

        @NotNull(message = "Les points d'arrêt sont obligatoires.")
        @Size(min = 2, message = "Le trajet doit contenir au moins deux points d'arrêt.")
        List<@NotNull @Valid PointArretRequest> pointsArret

) {
}