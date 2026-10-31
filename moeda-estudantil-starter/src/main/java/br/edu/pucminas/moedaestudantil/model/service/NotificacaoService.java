
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.infrastructure.email.MoedasRecebidasEvent;
import br.edu.pucminas.moedaestudantil.model.entity.Aluno;
import br.edu.pucminas.moedaestudantil.model.entity.Notificacao;
import br.edu.pucminas.moedaestudantil.model.repository.NotificacaoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacoes;
    private final ApplicationEventPublisher eventos;

    public NotificacaoService(
            NotificacaoRepository notificacoes,
            ApplicationEventPublisher eventos) {

        this.notificacoes = notificacoes;
        this.eventos = eventos;
    }

    public void registrarRecebimento(
            Aluno aluno,
            int quantidade,
            String motivo,
            String nomeProfessor) {

        Notificacao notificacao = new Notificacao();

        notificacao.setUsuario(aluno);
        notificacao.setTipo("RECEBIMENTO_MOEDAS");
        notificacao.setMensagem(
            "Voce recebeu " + quantidade +
            " moedas em sua conta."
        );

        notificacoes.save(notificacao);

        eventos.publishEvent(
            new MoedasRecebidasEvent(
                aluno.getEmail(),
                aluno.getNome(),
                nomeProfessor,
                quantidade,
                motivo
            )
        );
    }
}
