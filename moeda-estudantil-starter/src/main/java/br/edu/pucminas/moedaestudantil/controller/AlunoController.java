
package br.edu.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.AlunoRequest;
import br.edu.pucminas.moedaestudantil.dto.response.AlunoResponse;
import br.edu.pucminas.moedaestudantil.model.service.AlunoService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> criar(
            @Valid @RequestBody AlunoRequest dados) {

        AlunoResponse criado = service.criar(dados);

        return ResponseEntity
                .created(URI.create("/api/alunos/" + criado.id()))
                .body(criado);
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlunoRequest dados) {

        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
