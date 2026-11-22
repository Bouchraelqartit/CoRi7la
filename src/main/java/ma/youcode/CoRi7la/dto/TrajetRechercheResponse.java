package ma.youcode.CoRi7la.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetRechercheResponse(
        Long trajetId,
        Long conducteurId,
        String nomConducteur,
        String prenomConducteur,
        String villeMontee,
        String villeDescente,
        LocalDateTime heureMontee,
        LocalDateTime heureDescente,
        BigDecimal prixParPlace,
        Long placesRestantes
) {
}