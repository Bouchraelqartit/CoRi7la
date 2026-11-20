package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.TrajetPublicationRequest;
import ma.youcode.CoRi7la.dto.TrajetResponse;

public interface TrajetService {

    TrajetResponse publier(
            Long conducteurId,
            TrajetPublicationRequest request
    );

    TrajetResponse consulterParId(Long id);
}