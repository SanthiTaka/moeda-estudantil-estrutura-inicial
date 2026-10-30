
package br.edu.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.EmpresaParceiraRequest;
import br.edu.pucminas.moedaestudantil.dto.response.EmpresaParceiraResponse;
import br.edu.pucminas.moedaestudantil.model.service.EmpresaParceiraService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaParceiraController {

    private final EmpresaParceiraService service;

    public EmpresaParceiraController(EmpresaParceiraService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmpresaParceiraResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EmpresaParceiraResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<EmpresaParceiraResponse> criar(
            @Valid @RequestBody EmpresaParceiraRequest dados) {

        EmpresaParceiraResponse criado = service.criar(dados);

        return ResponseEntity
                .created(URI.create("/api/empresas/" + criado.id()))
                .body(criado);
    }

    @PutMapping("/{id}")
    public EmpresaParceiraResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaParceiraRequest dados) {

        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
