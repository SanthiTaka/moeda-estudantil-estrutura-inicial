
package br.edu.pucminas.moedaestudantil;

import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:auth_teste;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "APP_PROFESSOR_DEMO_ENABLED=false"
})
@AutoConfigureMockMvc
@Transactional
class CadastroIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AlunoRepository alunos;

    @Autowired
    private EmpresaParceiraRepository empresas;

    @Autowired
    private ProfessorRepository professores;

    @Autowired
    private InstituicaoRepository instituicoes;

    @Autowired
    private PasswordEncoder encoder;

    private Instituicao instituicao;

    private static final String SENHA = "SenhaTeste123!";

    @BeforeEach
    void preparar() {
        Instituicao nova = new Instituicao();
        nova.setNome("Instituicao de Teste");

        instituicao = instituicoes.saveAndFlush(nova);
    }

    private String jsonAluno(
            String nome,
            String login,
            String cpf,
            String rg) {

        return """
            {
              "nome": "%s",
              "email": "%s@example.com",
              "login": "%s",
              "senha": "%s",
              "cpf": "%s",
              "rg": "%s",
              "endereco": "Rua Teste, 100",
              "curso": "Engenharia de Software",
              "instituicaoId": %d
            }
            """.formatted(
                nome, login, login, SENHA,
                cpf, rg, instituicao.getId()
            );
    }

    private String jsonEmpresa(
            String nome,
            String login,
            String cnpj) {

        return """
            {
              "nome": "%s",
              "email": "%s@example.com",
              "login": "%s",
              "senha": "%s",
              "cnpj": "%s"
            }
            """.formatted(
                nome, login, login, SENHA, cnpj
            );
    }

    private Long obterId(MvcResult resultado) {
        String location = resultado.getResponse()
                .getHeader("Location");

        assertNotNull(location);

        return Long.parseLong(
            location.substring(location.lastIndexOf('/') + 1)
        );
    }

    private Long cadastrarAluno(
            String nome,
            String login,
            String cpf,
            String rg) throws Exception {

        MvcResult resultado = mvc.perform(
            post("/api/alunos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAluno(nome, login, cpf, rg))
        )
        .andExpect(status().isCreated())
        .andReturn();

        return obterId(resultado);
    }

    private Long cadastrarEmpresa(
            String nome,
            String login,
            String cnpj) throws Exception {

        MvcResult resultado = mvc.perform(
            post("/api/empresas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEmpresa(nome, login, cnpj))
        )
        .andExpect(status().isCreated())
        .andReturn();

        return obterId(resultado);
    }

    private MockHttpSession login(
            String usuario,
            String senha) throws Exception {

        MvcResult resultado = mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("login", usuario)
                .param("senha", senha)
        )
        .andExpect(status().isNoContent())
        .andReturn();

        MockHttpSession sessao =
            (MockHttpSession) resultado.getRequest()
                .getSession(false);

        assertNotNull(sessao);

        return sessao;
    }

    // TESTE 1 - CRUD ALUNO

    @Test
    void deveExecutarCrudCompletoDeAluno()
            throws Exception {

        Long id = cadastrarAluno(
            "Aluno Teste",
            "alunoteste",
            "52998224725",
            "RG12345"
        );

        assertTrue(alunos.existsById(id));

        MockHttpSession sessao =
            login("alunoteste", SENHA);

        mvc.perform(
            get("/api/alunos/{id}", id)
                .session(sessao)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome")
            .value("Aluno Teste"));

        mvc.perform(
            put("/api/alunos/{id}", id)
                .session(sessao)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAluno(
                    "Aluno Atualizado",
                    "alunoteste",
                    "52998224725",
                    "RG12345"
                ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome")
            .value("Aluno Atualizado"));

        mvc.perform(
            delete("/api/alunos/{id}", id)
                .session(sessao)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        assertFalse(alunos.existsById(id));
    }

    // TESTE 2 - CRUD EMPRESA

    @Test
    void deveExecutarCrudCompletoDeEmpresa()
            throws Exception {

        Long id = cadastrarEmpresa(
            "Empresa Teste",
            "empresateste",
            "11222333000181"
        );

        assertTrue(empresas.existsById(id));

        MockHttpSession sessao =
            login("empresateste", SENHA);

        mvc.perform(
            get("/api/empresas/{id}", id)
                .session(sessao)
        )
        .andExpect(status().isOk());

        mvc.perform(
            put("/api/empresas/{id}", id)
                .session(sessao)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEmpresa(
                    "Empresa Atualizada",
                    "empresateste",
                    "11222333000181"
                ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome")
            .value("Empresa Atualizada"));

        mvc.perform(
            delete("/api/empresas/{id}", id)
                .session(sessao)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        assertFalse(empresas.existsById(id));
    }

    // TESTE 3 - CPF DUPLICADO

    @Test
    void deveImpedirCpfDuplicado()
            throws Exception {

        cadastrarAluno(
            "Primeiro Aluno",
            "primeiro",
            "52998224725",
            "RG11111"
        );

        mvc.perform(
            post("/api/alunos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAluno(
                    "Segundo Aluno",
                    "segundo",
                    "52998224725",
                    "RG22222"
                ))
        )
        .andExpect(status().isConflict());
    }

    // TESTE 4 - CNPJ DUPLICADO

    @Test
    void deveImpedirCnpjDuplicado()
            throws Exception {

        cadastrarEmpresa(
            "Empresa Um",
            "empresaum",
            "11222333000181"
        );

        mvc.perform(
            post("/api/empresas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEmpresa(
                    "Empresa Dois",
                    "empresadois",
                    "11222333000181"
                ))
        )
        .andExpect(status().isConflict());
    }

    // TESTE 5 - AUTENTICACAO DOS PERFIS

    @Test
    void deveAutenticarOsTresPerfis()
            throws Exception {

        cadastrarAluno(
            "Aluno Login",
            "alunologin",
            "52998224725",
            "RG33333"
        );

        cadastrarEmpresa(
            "Empresa Login",
            "empresalogin",
            "11222333000181"
        );

        Professor professor = new Professor();

        professor.setNome("Professor Teste");
        professor.setEmail("professor@example.com");
        professor.setLogin("professorteste");
        professor.setSenhaHash(encoder.encode(SENHA));
        professor.setCpf("11144477735");
        professor.setDepartamento("Computacao");
        professor.setInstituicao(instituicao);

        professores.saveAndFlush(professor);

        MockHttpSession sessaoAluno =
            login("alunologin", SENHA);

        MockHttpSession sessaoEmpresa =
            login("empresalogin", SENHA);

        MockHttpSession sessaoProfessor =
            login("professorteste", SENHA);

        mvc.perform(
            get("/api/auth/me").session(sessaoAluno)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.perfil")
            .value("ALUNO"));

        mvc.perform(
            get("/api/auth/me").session(sessaoEmpresa)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.perfil")
            .value("EMPRESA"));

        mvc.perform(
            get("/api/auth/me").session(sessaoProfessor)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.perfil")
            .value("PROFESSOR"));
    }

    // TESTE 6 - ACESSO SEM LOGIN

    @Test
    void deveBloquearAcessoSemLogin()
            throws Exception {

        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/alunos"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/empresas"))
            .andExpect(status().isUnauthorized());
    }

    // TESTE 7 - SENHA INCORRETA

    @Test
    void deveRejeitarSenhaIncorreta()
            throws Exception {

        cadastrarAluno(
            "Aluno Senha",
            "alunosenha",
            "52998224725",
            "RG44444"
        );

        mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("login", "alunosenha")
                .param("senha", "SenhaErrada123")
        )
        .andExpect(status().isUnauthorized());
    }

    // TESTE 8 - PROTECAO DE OUTRO CADASTRO

    @Test
    void deveImpedirAcessoAoCadastroDeOutroAluno()
            throws Exception {

        cadastrarAluno(
            "Aluno Um",
            "alunoum",
            "52998224725",
            "RG55555"
        );

        Long outroId = cadastrarAluno(
            "Aluno Dois",
            "alunodois",
            "12312312387",
            "RG66666"
        );

        MockHttpSession sessao =
            login("alunoum", SENHA);

        mvc.perform(
            get("/api/alunos/{id}", outroId)
                .session(sessao)
        )
        .andExpect(status().isForbidden());
    }

    // TESTE 9 - CSRF OBRIGATORIO

    @Test
    void deveExigirTokenCsrf()
            throws Exception {

        mvc.perform(
            post("/api/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAluno(
                    "Sem CSRF",
                    "semcsrf",
                    "52998224725",
                    "RG77777"
                ))
        )
        .andExpect(status().isForbidden());
    }

    // TESTE 10 - PERMISSAO POR PERFIL

    @Test
    void deveBloquearAlunoNaAreaDeEmpresas()
            throws Exception {

        cadastrarAluno(
            "Aluno Perfil",
            "alunoperfil",
            "52998224725",
            "RG88888"
        );

        MockHttpSession sessao =
            login("alunoperfil", SENHA);

        mvc.perform(
            get("/api/empresas").session(sessao)
        )
        .andExpect(status().isForbidden());
    }
}
