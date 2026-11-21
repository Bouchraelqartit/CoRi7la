package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.dto.ReservationResponse;

public interface ReservationService {

    ReservationResponse reserver(
            Long passagerId,
            ReservationRequest request
    );
}