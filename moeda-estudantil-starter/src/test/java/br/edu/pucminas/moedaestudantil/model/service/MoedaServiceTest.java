
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.dto.request.EnviarMoedasRequest;
import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:teste_moedas;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "APP_PROFESSOR_DEMO_ENABLED=false"
})
@Transactional
class MoedaServiceTest {

    @Autowired private MoedaService moedas;
    @Autowired private CreditoSemestralService creditos;
    @Autowired private ExtratoService extratos;

    @Autowired private InstituicaoRepository instituicoes;
    @Autowired private ProfessorRepository professores;
    @Autowired private AlunoRepository alunos;

    private Professor professor;
    private Aluno aluno;

    @BeforeEach
    void preparar() {

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("PUC Teste");
        instituicao = instituicoes.save(instituicao);

        professor = new Professor();
        professor.setNome("Professor Teste");
        professor.setEmail("profmoedas@example.com");
        professor.setLogin("profmoedas");
        professor.setSenhaHash("hash-teste");
        professor.setCpf("11144477735");
        professor.setDepartamento("Computacao");
        professor.setInstituicao(instituicao);

        professor = professores.save(professor);

        aluno = new Aluno();
        aluno.setNome("Aluno Teste");
        aluno.setEmail("alunomoedas@example.com");
        aluno.setLogin("alunomoedas");
        aluno.setSenhaHash("hash-teste");
        aluno.setCpf("52998224725");
        aluno.setRg("RGTESTE001");
        aluno.setEndereco("Rua Teste");
        aluno.setCurso("Engenharia de Software");
        aluno.setInstituicao(instituicao);

        aluno = alunos.save(aluno);
    }

    @Test
    void deveCreditarTransferirERegistrarExtrato() {

        // Primeiro credito semestral
        assertTrue(
            creditos.creditarProfessor(professor.getId())
        );

        // Nao pode creditar duas vezes no semestre
        assertFalse(
            creditos.creditarProfessor(professor.getId())
        );

        // Professor transfere 150 moedas
        moedas.enviar(
            professor.getLogin(),
            new EnviarMoedasRequest(
                aluno.getId(),
                150,
                "Boa participacao em aula"
            )
        );

        var extratoProfessor =
            extratos.consultar(professor.getLogin());

        var extratoAluno =
            extratos.consultar(aluno.getLogin());

        assertEquals(850, extratoProfessor.saldo());
        assertEquals(150, extratoAluno.saldo());

        assertEquals(
            2,
            extratoProfessor.transacoes().size()
        );

        assertEquals(
            1,
            extratoAluno.transacoes().size()
        );
    }
}
