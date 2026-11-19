package ma.youcode.CoRi7la.controller;

import jakarta.validation.Valid;
import ma.youcode.CoRi7la.dto.PassagerRequest;
import ma.youcode.CoRi7la.dto.PassagerResponse;
import ma.youcode.CoRi7la.service.PassagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/passagers")
public class PassagerController {

    private final PassagerService service;

    public PassagerController(PassagerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PassagerResponse> creer(
            @Valid @RequestBody PassagerRequest request) {

        PassagerResponse response = service.creer(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassagerResponse> consulterParId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.consulterParId(id));
    }

    @GetMapping
    public ResponseEntity<List<PassagerResponse>> rechercher(
            @RequestParam(required = false) String email) {

        return ResponseEntity.ok(service.rechercher(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassagerResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PassagerRequest request) {

        return ResponseEntity.ok(service.modifier(id, request));
    }
}