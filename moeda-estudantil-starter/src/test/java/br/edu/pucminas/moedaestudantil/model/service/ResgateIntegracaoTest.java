
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:teste_resgates;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "APP_PROFESSOR_DEMO_ENABLED=false",
    "app.email.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
class ResgateIntegracaoTest {

    @Autowired private MockMvc mvc;

    @Autowired private InstituicaoRepository instituicoes;
    @Autowired private AlunoRepository alunos;
    @Autowired private EmpresaParceiraRepository empresas;
    @Autowired private ContaRepository contas;
    @Autowired private VantagemRepository vantagens;
    @Autowired private ResgateRepository resgates;
    @Autowired private TransacaoRepository transacoes;
    @Autowired private NotificacaoRepository notificacoes;

    private Aluno aluno;
    private Conta conta;
    private Vantagem vantagem;

    @BeforeEach
    void preparar() {

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituicao Resgates");

        instituicao = instituicoes.saveAndFlush(instituicao);

        aluno = new Aluno();
        aluno.setNome("Aluno Resgate");
        aluno.setEmail("alunoresgate@example.com");
        aluno.setLogin("alunoresgate");
        aluno.setSenhaHash("hash-teste");
        aluno.setCpf("52998224725");
        aluno.setRg("RGRESGATE001");
        aluno.setEndereco("Rua Teste");
        aluno.setCurso("Engenharia de Software");
        aluno.setInstituicao(instituicao);

        aluno = alunos.saveAndFlush(aluno);

        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome("Cafeteria Teste");
        empresa.setEmail("cafeteria@example.com");
        empresa.setLogin("cafeteriateste");
        empresa.setSenhaHash("hash-teste");
        empresa.setCnpj("11222333000181");

        empresa = empresas.saveAndFlush(empresa);

        conta = new Conta();
        conta.setUsuario(aluno);
        conta.setSaldo(150);

        conta = contas.saveAndFlush(conta);

        vantagem = new Vantagem();
        vantagem.setEmpresa(empresa);
        vantagem.setDescricao("Combo Cafe e Pao de Queijo");
        vantagem.setFoto("https://example.com/foto.jpg");
        vantagem.setCustoMoedas(100);

        vantagem = vantagens.saveAndFlush(vantagem);
    }

    private String requisicao() {
        return """
            {"vantagemId": %d}
            """.formatted(vantagem.getId());
    }

    @Test
    @WithMockUser(
        username = "alunoresgate",
        roles = "ALUNO"
    )
    void deveResgatarEDescontarMoedas() throws Exception {

        mvc.perform(
            post("/api/resgates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requisicao())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.codigoCupom").isNotEmpty())
        .andExpect(jsonPath("$.custoMoedas").value(100));

        Conta atualizada = contas.findById(
            conta.getId()
        ).orElseThrow();

        assertEquals(50, atualizada.getSaldo());
        assertEquals(1, resgates.count());

        var movimentacoes = transacoes
            .findByConta_IdOrderByDataDescIdDesc(
                conta.getId()
            );

        assertEquals(1, movimentacoes.size());
        assertEquals(-100, movimentacoes.get(0).getValor());
        assertEquals("RESGATE", movimentacoes.get(0).getTipo());

        // Aluno e empresa receberam notificacoes
        assertEquals(2, notificacoes.count());
    }

    @Test
    @WithMockUser(
        username = "alunoresgate",
        roles = "ALUNO"
    )
    void naoDeveResgatarSemSaldo() throws Exception {

        conta.setSaldo(50);
        contas.saveAndFlush(conta);

        mvc.perform(
            post("/api/resgates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requisicao())
        )
        .andExpect(status().isBadRequest());

        assertEquals(
            50,
            contas.findById(conta.getId())
                .orElseThrow().getSaldo()
        );

        assertEquals(0, resgates.count());
        assertEquals(0, transacoes.count());
        assertEquals(0, notificacoes.count());
    }

    @Test
    @WithMockUser(
        username = "cafeteriateste",
        roles = "EMPRESA"
    )
    void empresaNaoPodeResgatar() throws Exception {

        mvc.perform(
            post("/api/resgates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requisicao())
        )
        .andExpect(status().isForbidden());

        assertEquals(0, resgates.count());
    }

    @Test
    @WithMockUser(
        username = "alunoresgate",
        roles = "ALUNO"
    )
    void deveConsultarOsPropriosCupons() throws Exception {

        mvc.perform(
            post("/api/resgates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requisicao())
        )
        .andExpect(status().isCreated());

        mvc.perform(
            get("/api/resgates/me")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].descricao")
            .value("Combo Cafe e Pao de Queijo"))
        .andExpect(jsonPath("$[0].codigoCupom")
            .isNotEmpty());
    }
}
