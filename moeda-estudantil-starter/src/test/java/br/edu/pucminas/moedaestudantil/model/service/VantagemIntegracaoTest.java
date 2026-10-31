
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:teste_vantagens;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "APP_PROFESSOR_DEMO_ENABLED=false"
})
@AutoConfigureMockMvc
@Transactional
class VantagemIntegracaoTest {

    @Autowired private MockMvc mvc;
    @Autowired private EmpresaParceiraRepository empresas;
    @Autowired private VantagemRepository vantagens;

    private EmpresaParceira empresa1;

    @BeforeEach
    void preparar() {
        empresa1 = criarEmpresa("empresa1");
        criarEmpresa("empresa2");
    }

    private EmpresaParceira criarEmpresa(String login) {
        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome(login);
        empresa.setEmail(login + "@example.com");
        empresa.setLogin(login);
        empresa.setSenhaHash("hash-teste");

        empresa.setCnpj(
            login.equals("empresa1")
                ? "11222333000181"
                : "00000000E08G12"
        );

        return empresas.saveAndFlush(empresa);
    }

    private String dados(String descricao, int custo) {
        return """
            {
              "descricao": "%s",
              "foto": "https://example.com/foto.jpg",
              "custoMoedas": %d
            }
            """.formatted(descricao, custo);
    }

    @Test
    @WithMockUser(username = "empresa1", roles = "EMPRESA")
    void deveExecutarCrudDeVantagem() throws Exception {

        MvcResult criado = mvc.perform(
            post("/api/vantagens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dados("Combo de Cafe", 100))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.custoMoedas").value(100))
        .andReturn();

        String location = criado.getResponse()
            .getHeader("Location");

        assertNotNull(location);

        Long id = Long.parseLong(
            location.substring(location.lastIndexOf('/') + 1)
        );

        mvc.perform(get("/api/vantagens/minhas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(
            put("/api/vantagens/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dados("Combo Atualizado", 120))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.custoMoedas").value(120));

        mvc.perform(
            delete("/api/vantagens/{id}", id)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        assertFalse(vantagens.existsById(id));
    }

    @Test
    @WithMockUser(username = "empresa2", roles = "EMPRESA")
    void naoDeveEditarVantagemDeOutraEmpresa()
            throws Exception {

        Vantagem vantagem = new Vantagem();
        vantagem.setEmpresa(empresa1);
        vantagem.setDescricao("Desconto de Teste");
        vantagem.setFoto("https://example.com/foto.jpg");
        vantagem.setCustoMoedas(80);

        vantagem = vantagens.saveAndFlush(vantagem);

        mvc.perform(
            put("/api/vantagens/{id}", vantagem.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dados("Alteracao Indevida", 20))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "aluno", roles = "ALUNO")
    void alunoPodeConsultarMasNaoCadastrar()
            throws Exception {

        mvc.perform(get("/api/vantagens"))
            .andExpect(status().isOk());

        mvc.perform(
            post("/api/vantagens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dados("Vantagem Indevida", 50))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "empresa1", roles = "EMPRESA")
    void naoDeveAceitarCustoInvalido() throws Exception {

        mvc.perform(
            post("/api/vantagens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dados("Vantagem Invalida", -10))
        )
        .andExpect(status().isBadRequest());
    }
}
