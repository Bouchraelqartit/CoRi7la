package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.dto.ReservationResponse;
import ma.youcode.CoRi7la.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/passagers/{passagerId}/reservations")
    public ResponseEntity<List<ReservationResponse>> listerParPassager(
            @PathVariable("passagerId") Long passagerId
    ) {
        return ResponseEntity.ok(service.listerParPassager(passagerId));
    }

    @GetMapping("/conducteurs/{conducteurId}/trajets/{trajetId}/reservations")
    public ResponseEntity<List<ReservationResponse>> listerParTrajetConducteur(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("trajetId") Long trajetId
    ) {
        return ResponseEntity.ok(
                service.listerParTrajetConducteur(conducteurId, trajetId)
        );
    }

    @PatchMapping("/passagers/{passagerId}/reservations/{reservationId}/annulation")
    public ResponseEntity<ReservationResponse> annuler(
            @PathVariable("passagerId") Long passagerId,
            @PathVariable("reservationId") Long reservationId
    ) {
        return ResponseEntity.ok(
                service.annuler(passagerId, reservationId)
        );
    }


}