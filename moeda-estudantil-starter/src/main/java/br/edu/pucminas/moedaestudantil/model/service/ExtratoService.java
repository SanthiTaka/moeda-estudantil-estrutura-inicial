
package br.edu.pucminas.moedaestudantil.model.service;

import java.util.NoSuchElementException;

import br.edu.pucminas.moedaestudantil.dto.response.*;
import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtratoService {

    private final UsuarioRepository usuarios;
    private final TransacaoRepository transacoes;
    private final ContaService contaService;

    public ExtratoService(
            UsuarioRepository usuarios,
            TransacaoRepository transacoes,
            ContaService contaService) {

        this.usuarios = usuarios;
        this.transacoes = transacoes;
        this.contaService = contaService;
    }

    @Transactional
    public ExtratoResponse consultar(String login) {

        Usuario usuario = usuarios
            .findByLoginIgnoreCase(login)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Usuario nao encontrado."
                )
            );

        if (!(usuario instanceof Aluno)
                && !(usuario instanceof Professor)) {
            throw new AccessDeniedException(
                "Este perfil nao possui extrato."
            );
        }

        Conta conta = contaService.obterOuCriar(usuario);

        var movimentacoes = transacoes
            .findByConta_IdOrderByDataDescIdDesc(
                conta.getId()
            )
            .stream()
            .map(t -> new TransacaoResponse(
                t.getId(),
                t.getData(),
                t.getTipo(),
                t.getValor(),
                t.getMotivo()
            ))
            .toList();

        return new ExtratoResponse(
            conta.getSaldo(),
            movimentacoes
        );
    }
}
