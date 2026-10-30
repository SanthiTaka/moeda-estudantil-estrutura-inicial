
package br.edu.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.AlunoRequest;
import br.edu.pucminas.moedaestudantil.dto.response.AlunoResponse;
import br.edu.pucminas.moedaestudantil.model.service.AlunoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> criar(
            @Valid @RequestBody AlunoRequest dados) {

        AlunoResponse criado = service.criar(dados);

        return ResponseEntity
                .created(URI.create("/api/alunos/" + criado.id()))
                .body(criado);
    }

    @GetMapping
    public List<AlunoResponse> listar(
            Authentication auth) {

        // Nesta fase, cada aluno lista apenas seu cadastro.
        return service.listar().stream()
                .filter(a -> a.login()
                    .equalsIgnoreCase(auth.getName()))
                .toList();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(
            @PathVariable Long id,
            Authentication auth) {

        verificarDono(id, auth);
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlunoRequest dados,
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
        AlunoResponse aluno = service.buscar(id);

        if (!aluno.login()
                .equalsIgnoreCase(auth.getName())) {

            throw new AccessDeniedException(
                "Voce nao pode acessar outro cadastro."
            );
        }
    }
}
