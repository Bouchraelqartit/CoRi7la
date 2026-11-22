package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.TrajetPublicationRequest;
import ma.youcode.CoRi7la.dto.TrajetRechercheResponse;
import ma.youcode.CoRi7la.dto.TrajetResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TrajetService {

    TrajetResponse publier(
            Long conducteurId,
            TrajetPublicationRequest request
    );

    TrajetResponse consulterParId(Long id);

    TrajetResponse annuler(Long conducteurId, Long trajetId);

    TrajetResponse terminer(Long conducteurId, Long trajetId);

    Page<TrajetRechercheResponse> rechercher(
            String villeMontee,
            String villeDescente,
            LocalDate date,
            int nombrePlaces,
            BigDecimal prixMax,
            int page,
            int taille
    );
}