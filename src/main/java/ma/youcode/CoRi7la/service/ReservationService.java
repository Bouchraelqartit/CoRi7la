package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.dto.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse reserver(
            Long passagerId,
            ReservationRequest request
    );

    List<ReservationResponse> listerParPassager(Long passagerId);

    List<ReservationResponse> listerParTrajetConducteur(
            Long conducteurId,
            Long trajetId
    );
}