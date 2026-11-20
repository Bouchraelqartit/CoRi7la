package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.VehiculeRequest;
import ma.youcode.CoRi7la.dto.VehiculeResponse;
import ma.youcode.CoRi7la.service.VehiculeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @PostMapping("/conducteurs/{conducteurId}/vehicules")
    public ResponseEntity<VehiculeResponse> ajouter(
            @PathVariable Long conducteurId,
            @Valid @RequestBody VehiculeRequest request) {

        VehiculeResponse response = service.ajouter(conducteurId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/vehicules/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/vehicules/{id}")
    public ResponseEntity<VehiculeResponse> consulterParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.consulterParId(id));
    }

    @GetMapping("/conducteurs/{conducteurId}/vehicules")
    public ResponseEntity<List<VehiculeResponse>> listerParConducteur(
            @PathVariable Long conducteurId) {

        return ResponseEntity.ok(
                service.listerParConducteur(conducteurId)
        );
    }

    @PutMapping("/vehicules/{id}")
    public ResponseEntity<VehiculeResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeRequest request) {

        return ResponseEntity.ok(service.modifier(id, request));
    }

    @DeleteMapping("/vehicules/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {

        service.supprimer(id);

        return ResponseEntity.noContent().build();
    }
}