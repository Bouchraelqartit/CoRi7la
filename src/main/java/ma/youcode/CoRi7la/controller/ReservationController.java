package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.dto.ReservationResponse;
import ma.youcode.CoRi7la.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping("/passagers/{passagerId}/reservations")
    public ResponseEntity<ReservationResponse> reserver(
            @PathVariable("passagerId") Long passagerId,
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResponse response =
                service.reserver(passagerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}