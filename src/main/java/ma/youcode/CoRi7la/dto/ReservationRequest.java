package ma.youcode.CoRi7la.dto;

import jakarta.validation.constraints.*;

public record ReservationRequest(

        @NotNull(message = "Le trajet est obligatoire.")
        @Positive(message = "L'identifiant du trajet doit être positif.")
        Long trajetId,

        @NotNull(message = "Le point de montée est obligatoire.")
        @Positive(message = "L'identifiant du point de montée doit être positif.")
        Long pointMonteeId,

        @NotNull(message = "Le point de descente est obligatoire.")
        @Positive(message = "L'identifiant du point de descente doit être positif.")
        Long pointDescenteId,

        @NotNull(message = "Le nombre de places est obligatoire.")
        @Min(value = 1, message = "Il faut réserver au moins une place.")
        @Max(value = 8, message = "Il est impossible de réserver plus de 8 places.")
        Integer nombrePlaces

) {
}