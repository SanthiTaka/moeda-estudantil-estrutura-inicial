
package br.edu.pucminas.moedaestudantil.controller;

import java.util.List;

import br.edu.pucminas.moedaestudantil.dto.request.EnviarMoedasRequest;
import br.edu.pucminas.moedaestudantil.dto.response.AlunoDestinoResponse;
import br.edu.pucminas.moedaestudantil.model.service.MoedaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moedas")
public class MoedaController {

    private final MoedaService service;

    public MoedaController(MoedaService service) {
        this.service = service;
    }

    @GetMapping("/alunos")
    public List<AlunoDestinoResponse> listarAlunos() {
        return service.listarAlunos();
    }

    @PostMapping("/enviar")
    public ResponseEntity<Void> enviar(
            Authentication authentication,
            @Valid @RequestBody EnviarMoedasRequest dados) {

        service.enviar(authentication.getName(), dados);

        return ResponseEntity.noContent().build();
    }
}
