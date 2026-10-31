
package br.edu.pucminas.moedaestudantil.infrastructure.scheduling;

import br.edu.pucminas.moedaestudantil.model.repository.ProfessorRepository;
import br.edu.pucminas.moedaestudantil.model.service.CreditoSemestralService;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;

@Configuration
@EnableScheduling
public class CreditoSemestralJob {

    private final ProfessorRepository professores;
    private final CreditoSemestralService creditos;

    public CreditoSemestralJob(
            ProfessorRepository professores,
            CreditoSemestralService creditos) {

        this.professores = professores;
        this.creditos = creditos;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        executar();
    }

    // Executa diariamente para contemplar
    // novos semestres e novos professores.
    @Scheduled(
        cron = "0 10 0 * * *",
        zone = "America/Sao_Paulo"
    )
    public void executar() {

        professores.findAll().forEach(professor -> {
            creditos.creditarProfessor(professor.getId());
        });
    }
}
