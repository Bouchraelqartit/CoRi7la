package ma.youcode.CoRi7la.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretRequest(

        @NotBlank(message = "La ville est obligatoire.")
        @Size(max = 255, message = "La ville ne doit pas dépasser 255 caractères.")
        String ville,

        @NotNull(message = "L'heure de passage est obligatoire.")
        @Future(message = "L'heure de passage doit être dans le futur.")
        LocalDateTime heurePassage,

        @NotNull(message = "Le prix depuis le départ est obligatoire.")
        @DecimalMin(value = "0.00", message = "Le prix ne peut pas être négatif.")
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Le prix doit contenir au maximum 8 chiffres entiers et 2 décimales."
        )
        BigDecimal prixDepuisDepart

) {
}