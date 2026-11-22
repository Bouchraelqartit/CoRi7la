package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.TrajetPublicationRequest;
import ma.youcode.CoRi7la.dto.TrajetResponse;
import ma.youcode.CoRi7la.service.TrajetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class TrajetController {

    private final TrajetService service;

    public TrajetController(TrajetService service) {
        this.service = service;
    }

    @PostMapping("/conducteurs/{conducteurId}/trajets")
    public ResponseEntity<TrajetResponse> publier(
            @PathVariable("conducteurId") Long conducteurId,
            @Valid @RequestBody TrajetPublicationRequest request
    ) {
        TrajetResponse response = service.publier(conducteurId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/trajets/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/trajets/{id}")
    public ResponseEntity<TrajetResponse> consulterParId(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(service.consulterParId(id));
    }

    @PatchMapping("/conducteurs/{conducteurId}/trajets/{trajetId}/annulation")
    public ResponseEntity<TrajetResponse> annuler(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("trajetId") Long trajetId
    ) {
        return ResponseEntity.ok(
                service.annuler(conducteurId, trajetId)
        );
    }

    @PatchMapping("/conducteurs/{conducteurId}/trajets/{trajetId}/terminaison")
    public ResponseEntity<TrajetResponse> terminer(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("trajetId") Long trajetId
    ) {
        return ResponseEntity.ok(
                service.terminer(conducteurId, trajetId)
        );
    }
}