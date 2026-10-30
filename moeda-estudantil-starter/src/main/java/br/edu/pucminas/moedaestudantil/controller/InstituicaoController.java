
package br.edu.pucminas.moedaestudantil.controller;

import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.response.InstituicaoResponse;
import br.edu.pucminas.moedaestudantil.model.repository.InstituicaoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoController {

    private final InstituicaoRepository repository;

    public InstituicaoController(InstituicaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<InstituicaoResponse> listar() {
        return repository.findAll(Sort.by("nome"))
                .stream()
                .map(i -> new InstituicaoResponse(
                        i.getId(),
                        i.getNome()
                ))
                .toList();
    }
}
