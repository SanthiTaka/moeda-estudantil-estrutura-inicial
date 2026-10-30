
package br.edu.pucminas.moedaestudantil.config;

import br.edu.pucminas.moedaestudantil.model.entity.Instituicao;
import br.edu.pucminas.moedaestudantil.model.repository.InstituicaoRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadosIniciaisConfig {

    @Bean
    CommandLineRunner criarInstituicaoDeExemplo(
            InstituicaoRepository repository) {

        return args -> {
            if (repository.count() == 0) {
                Instituicao instituicao = new Instituicao();
                instituicao.setNome("PUC Minas");
                repository.save(instituicao);
            }
        };
    }
}
