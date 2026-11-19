package ma.youcode.CoRi7la.dto;

public record VehiculeResponse(
        Long id,
        String marque,
        String modele,
        String couleur,
        String immatriculation,
        int capacite,
        Long conducteurId
) {
}