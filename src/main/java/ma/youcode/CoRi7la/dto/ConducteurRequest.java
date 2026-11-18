package ma.youcode.CoRi7la.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConducteurRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255)
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 255)
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email est invalide")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Size(max = 255)
        String telephone,

        @NotBlank(message = "Le numéro de permis est obligatoire")
        @Size(max = 255)
        String numeroPermis
) {
}