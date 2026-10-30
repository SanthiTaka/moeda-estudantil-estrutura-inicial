
package br.edu.pucminas.moedaestudantil.controller;

import java.util.Map;

import br.edu.pucminas.moedaestudantil.dto.response.PerfilResponse;
import br.edu.pucminas.moedaestudantil.model.service.AutenticacaoService;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService service;

    public AutenticacaoController(
            AutenticacaoService service) {
        this.service = service;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {

        // Forca a geracao do token para a interface web.
        token.getToken();

        return Map.of(
            "mensagem", "Protecao CSRF habilitada."
        );
    }

    @GetMapping("/me")
    public PerfilResponse meuPerfil(
            Authentication authentication) {

        return service.identificar(
            authentication.getName()
        );
    }
}
