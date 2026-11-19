package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.ConducteurRequest;
import ma.youcode.CoRi7la.dto.ConducteurResponse;
import ma.youcode.CoRi7la.service.ConducteurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
public class ConducteurController {

    private final ConducteurService service;

    public ConducteurController(ConducteurService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConducteurResponse> creer(
            @Valid @RequestBody ConducteurRequest request) {

        ConducteurResponse response = service.creer(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConducteurResponse> consulterParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.consulterParId(id));
    }

    @GetMapping
    public ResponseEntity<List<ConducteurResponse>> rechercher(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email) {

        return ResponseEntity.ok(service.rechercher(nom, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConducteurResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurRequest request) {

        return ResponseEntity.ok(service.modifier(id, request));
    }
}