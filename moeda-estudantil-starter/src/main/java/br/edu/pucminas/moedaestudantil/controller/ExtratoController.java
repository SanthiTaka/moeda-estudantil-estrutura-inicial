
package br.edu.pucminas.moedaestudantil.controller;

import br.edu.pucminas.moedaestudantil.dto.response.ExtratoResponse;
import br.edu.pucminas.moedaestudantil.model.service.ExtratoService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/extratos")
public class ExtratoController {

    private final ExtratoService service;

    public ExtratoController(ExtratoService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ExtratoResponse meuExtrato(
            Authentication authentication) {

        return service.consultar(authentication.getName());
    }
}
