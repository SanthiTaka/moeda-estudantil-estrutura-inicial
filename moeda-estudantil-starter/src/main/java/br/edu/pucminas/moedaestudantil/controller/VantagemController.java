
package br.edu.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.VantagemRequest;
import br.edu.pucminas.moedaestudantil.dto.response.VantagemResponse;
import br.edu.pucminas.moedaestudantil.model.service.VantagemService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vantagens")
public class VantagemController {

    private final VantagemService service;

    public VantagemController(VantagemService service) {
        this.service = service;
    }

    @GetMapping
    public List<VantagemResponse> listar() {
        return service.listar();
    }

    @GetMapping("/minhas")
    public List<VantagemResponse> minhas(
            Authentication authentication) {

        return service.minhas(authentication.getName());
    }

    @GetMapping("/{id}")
    public VantagemResponse buscar(
            @PathVariable Long id) {

        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<VantagemResponse> criar(
            Authentication authentication,
            @Valid @RequestBody VantagemRequest dados) {

        VantagemResponse criada = service.criar(
            authentication.getName(), dados
        );

        return ResponseEntity
            .created(URI.create(
                "/api/vantagens/" + criada.id()
            ))
            .body(criada);
    }

    @PutMapping("/{id}")
    public VantagemResponse atualizar(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody VantagemRequest dados) {

        return service.atualizar(
            authentication.getName(), id, dados
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            Authentication authentication,
            @PathVariable Long id) {

        service.excluir(authentication.getName(), id);

        return ResponseEntity.noContent().build();
    }
}
