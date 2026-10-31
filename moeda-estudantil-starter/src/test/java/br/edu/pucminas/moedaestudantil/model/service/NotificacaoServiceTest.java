
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.infrastructure.email.MoedasRecebidasEvent;
import br.edu.pucminas.moedaestudantil.model.entity.Aluno;
import br.edu.pucminas.moedaestudantil.model.entity.Notificacao;
import br.edu.pucminas.moedaestudantil.model.repository.NotificacaoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacoes;

    @Mock
    private ApplicationEventPublisher eventos;

    @InjectMocks
    private NotificacaoService service;

    @Test
    void deveRegistrarNotificacaoEPublicarEvento() {

        Aluno aluno = new Aluno();
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@example.com");

        service.registrarRecebimento(
            aluno,
            150,
            "Excelente participacao",
            "Professor Teste"
        );

        ArgumentCaptor<Notificacao> notificacao =
            ArgumentCaptor.forClass(Notificacao.class);

        verify(notificacoes).save(notificacao.capture());

        assertEquals(
            "RECEBIMENTO_MOEDAS",
            notificacao.getValue().getTipo()
        );

        ArgumentCaptor<MoedasRecebidasEvent> evento =
            ArgumentCaptor.forClass(
                MoedasRecebidasEvent.class
            );

        verify(eventos).publishEvent(evento.capture());

        assertEquals(
            "aluno@example.com",
            evento.getValue().emailAluno()
        );

        assertEquals(
            150,
            evento.getValue().quantidade()
        );

        assertEquals(
            "Professor Teste",
            evento.getValue().nomeProfessor()
        );
    }
}
