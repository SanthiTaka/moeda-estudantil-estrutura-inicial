
package br.edu.pucminas.moedaestudantil;

import br.edu.pucminas.moedaestudantil.model.entity.Instituicao;
import br.edu.pucminas.moedaestudantil.model.repository.AlunoRepository;
import br.edu.pucminas.moedaestudantil.model.repository.EmpresaParceiraRepository;
import br.edu.pucminas.moedaestudantil.model.repository.InstituicaoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = {
    "APP_DEV_PASSWORD=SenhaTesteIntegracao123!",
    "spring.datasource.url=jdbc:h2:mem:moeda_integracao;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})

@AutoConfigureMockMvc
@Transactional
class CadastroIntegracaoTest {

    private static final String AUTH = "Basic " +
        Base64.getEncoder().encodeToString(
            "user:SenhaTesteIntegracao123!"
                .getBytes(StandardCharsets.UTF_8)
        );

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AlunoRepository alunos;

    @Autowired
    private EmpresaParceiraRepository empresas;

    @Autowired
    private InstituicaoRepository instituicoes;

    private Instituicao instituicao;

    @BeforeEach
    void preparar() {
        Instituicao nova = new Instituicao();
        nova.setNome("Instituicao Teste Automatica");
        instituicao = instituicoes.saveAndFlush(nova);
    }

    private String jsonAluno(
            String nome,
            String email,
            String login,
            String cpf,
            String rg) {

        return """
            {
              "nome": "%s",
              "email": "%s",
              "login": "%s",
              "senha": "Teste12345!",
              "cpf": "%s",
              "rg": "%s",
              "endereco": "Rua Teste, 100",
              "curso": "Engenharia de Software",
              "instituicaoId": %d
            }
            """.formatted(
                nome, email, login, cpf, rg,
                instituicao.getId()
            );
    }

    private String jsonEmpresa(
            String nome,
            String email,
            String login,
            String cnpj) {

        return """
            {
              "nome": "%s",
              "email": "%s",
              "login": "%s",
              "senha": "Teste12345!",
              "cnpj": "%s"
            }
            """.formatted(nome, email, login, cnpj);
    }

    private Long obterId(MvcResult resultado) {
        String local = resultado.getResponse()
                .getHeader("Location");

        assertNotNull(local);

        return Long.parseLong(
                local.substring(local.lastIndexOf('/') + 1)
        );
    }

    @Test
    void deveExecutarCrudCompletoDeAluno()
            throws Exception {

        // CREATE
        MvcResult criado = mvc.perform(
                post("/api/alunos")
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonAluno(
                        "Aluno Teste",
                        "aluno@teste.com",
                        "alunoteste",
                        "52998224725",
                        "MG1234567"
                    ))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome")
                    .value("Aluno Teste"))
            .andReturn();

        Long id = obterId(criado);

        assertTrue(alunos.existsById(id));

        // READ
        mvc.perform(
                get("/api/alunos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf")
                    .value("52998224725"));

        // UPDATE
        mvc.perform(
                put("/api/alunos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonAluno(
                        "Aluno Atualizado",
                        "aluno@teste.com",
                        "alunoteste",
                        "52998224725",
                        "MG1234567"
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome")
                    .value("Aluno Atualizado"));

        // DELETE
        mvc.perform(
                delete("/api/alunos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
            )
            .andExpect(status().isNoContent());

        assertFalse(alunos.existsById(id));
    }

    @Test
    void deveExecutarCrudCompletoDeEmpresa()
            throws Exception {

        // CREATE
        MvcResult criado = mvc.perform(
                post("/api/empresas")
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEmpresa(
                        "Empresa Teste",
                        "empresa@teste.com",
                        "empresateste",
                        "11222333000181"
                    ))
            )
            .andExpect(status().isCreated())
            .andReturn();

        Long id = obterId(criado);

        assertTrue(empresas.existsById(id));

        // READ
        mvc.perform(
                get("/api/empresas/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cnpj")
                    .value("11222333000181"));

        // UPDATE
        mvc.perform(
                put("/api/empresas/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEmpresa(
                        "Empresa Atualizada",
                        "empresa@teste.com",
                        "empresateste",
                        "11222333000181"
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome")
                    .value("Empresa Atualizada"));

        // DELETE
        mvc.perform(
                delete("/api/empresas/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
            )
            .andExpect(status().isNoContent());

        assertFalse(empresas.existsById(id));
    }

    @Test
    void deveImpedirCpfDuplicado() throws Exception {

        mvc.perform(
                post("/api/alunos")
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonAluno(
                        "Primeiro Aluno",
                        "primeiro@teste.com",
                        "primeiro",
                        "52998224725",
                        "RG12345"
                    ))
            )
            .andExpect(status().isCreated());

        mvc.perform(
                post("/api/alunos")
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonAluno(
                        "Segundo Aluno",
                        "segundo@teste.com",
                        "segundo",
                        "52998224725",
                        "RG67890"
                    ))
            )
            .andExpect(status().isConflict());
    }

    @Test
    void deveImpedirCnpjDuplicado() throws Exception {

        mvc.perform(
                post("/api/empresas")
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEmpresa(
                        "Primeira Empresa",
                        "primeira@teste.com",
                        "primeira",
                        "11222333000181"
                    ))
            )
            .andExpect(status().isCreated());

        mvc.perform(
                post("/api/empresas")
                    .header(HttpHeaders.AUTHORIZATION, AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEmpresa(
                        "Segunda Empresa",
                        "segunda@teste.com",
                        "segunda",
                        "11222333000181"
                    ))
            )
            .andExpect(status().isConflict());
    }

    @Test
    void deveBloquearAcessoSemLogin() throws Exception {

        mvc.perform(get("/api/alunos"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/empresas"))
            .andExpect(status().isUnauthorized());
    }
}
