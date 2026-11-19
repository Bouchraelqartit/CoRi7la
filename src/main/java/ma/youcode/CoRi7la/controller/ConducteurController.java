package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.ConducteurRequest;
import ma.youcode.CoRi7la.dto.ConducteurResponse;
import ma.youcode.CoRi7la.service.ConducteurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
}