
package br.edu.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.EmpresaParceiraRequest;
import br.edu.pucminas.moedaestudantil.dto.response.EmpresaParceiraResponse;
import br.edu.pucminas.moedaestudantil.model.service.EmpresaParceiraService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaParceiraController {

    private final EmpresaParceiraService service;

    public EmpresaParceiraController(
            EmpresaParceiraService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmpresaParceiraResponse> criar(
            @Valid @RequestBody EmpresaParceiraRequest dados) {

        EmpresaParceiraResponse criado = service.criar(dados);

        return ResponseEntity
                .created(URI.create("/api/empresas/" + criado.id()))
                .body(criado);
    }

    @GetMapping
    public List<EmpresaParceiraResponse> listar(
            Authentication auth) {

        return service.listar().stream()
                .filter(e -> e.login()
                    .equalsIgnoreCase(auth.getName()))
                .toList();
    }

    @GetMapping("/{id}")
    public EmpresaParceiraResponse buscar(
            @PathVariable Long id,
            Authentication auth) {

        verificarDono(id, auth);
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public EmpresaParceiraResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaParceiraRequest dados,
            Authentication auth) {

        verificarDono(id, auth);

        if (!dados.login().trim()
                .equalsIgnoreCase(auth.getName())) {
            throw new IllegalArgumentException(
                "O login nao pode ser alterado nesta etapa."
            );
        }

        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            Authentication auth) {

        verificarDono(id, auth);
        service.excluir(id);

        return ResponseEntity.noContent().build();
    }

    private void verificarDono(Long id, Authentication auth) {

        EmpresaParceiraResponse empresa = service.buscar(id);

        if (!empresa.login()
                .equalsIgnoreCase(auth.getName())) {

            throw new AccessDeniedException(
                "Voce nao pode acessar outro cadastro."
            );
        }
    }
}
