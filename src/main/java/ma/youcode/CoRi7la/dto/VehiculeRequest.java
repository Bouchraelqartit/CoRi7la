package ma.youcode.CoRi7la.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculeRequest(

        @NotBlank(message = "La marque est obligatoire")
        @Size(max = 255)
        String marque,

        @NotBlank(message = "Le modèle est obligatoire")
        @Size(max = 255)
        String modele,

        @NotBlank(message = "La couleur est obligatoire")
        @Size(max = 255)
        String couleur,

        @NotBlank(message = "L'immatriculation est obligatoire")
        @Size(max = 255)
        String immatriculation,

        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité minimale est de 1 place")
        @Max(value = 8, message = "La capacité maximale est de 8 places")
        Integer capacite
) {
}