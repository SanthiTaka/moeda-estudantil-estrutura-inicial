
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.NotificacaoRepository;

import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacoes;

    public NotificacaoService(
            NotificacaoRepository notificacoes) {
        this.notificacoes = notificacoes;
    }

    public void registrarRecebimento(
            Aluno aluno, int quantidade) {

        Notificacao notificacao = new Notificacao();

        notificacao.setUsuario(aluno);
        notificacao.setTipo("RECEBIMENTO_MOEDAS");
        notificacao.setMensagem(
            "Voce recebeu " + quantidade +
            " moedas em sua conta."
        );

        notificacoes.save(notificacao);
    }
}
