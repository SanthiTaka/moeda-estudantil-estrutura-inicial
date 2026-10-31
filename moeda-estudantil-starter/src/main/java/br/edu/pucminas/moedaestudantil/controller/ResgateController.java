
package br.edu.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.ResgateRequest;
import br.edu.pucminas.moedaestudantil.dto.response.ResgateResponse;
import br.edu.pucminas.moedaestudantil.model.service.ResgateService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resgates")
public class ResgateController {

    private final ResgateService service;

    public ResgateController(ResgateService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResgateResponse> resgatar(
            Authentication authentication,
            @Valid @RequestBody ResgateRequest dados) {

        ResgateResponse resgate = service.resgatar(
            authentication.getName(),
            dados
        );

        return ResponseEntity
            .created(URI.create(
                "/api/resgates/" + resgate.id()
            ))
            .body(resgate);
    }

    @GetMapping("/me")
    public List<ResgateResponse> meusResgates(
            Authentication authentication) {

        return service.meusResgates(
            authentication.getName()
        );
    }
}
