package br.edu.pucminas.moedaestudantil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.pucminas.moedaestudantil.model.entity.Instituicao;
import br.edu.pucminas.moedaestudantil.model.entity.Professor;
import br.edu.pucminas.moedaestudantil.model.repository.InstituicaoRepository;
import br.edu.pucminas.moedaestudantil.model.repository.ProfessorRepository;
import br.edu.pucminas.moedaestudantil.model.repository.UsuarioRepository;

@Configuration
public class ProfessorDemoConfig {

    @Bean
    @ConditionalOnProperty(
            name = "APP_PROFESSOR_DEMO_ENABLED",
            havingValue = "true"
    )
    CommandLineRunner cadastrarProfessorDemo(
            ProfessorRepository professores,
            UsuarioRepository usuarios,
            InstituicaoRepository instituicoes,
            PasswordEncoder encoder,
            @Value("${APP_PROFESSOR_DEMO_SENHA:}") String senha) {

        return args -> {

            if (senha == null || senha.length() < 8) {
                throw new IllegalStateException(
                        "Configure APP_PROFESSOR_DEMO_SENHA "
                        + "com pelo menos 8 caracteres."
                );
            }

            String login = "professordemo";

            // Evita cadastrar o mesmo professor novamente
            var existente = usuarios.findByLoginIgnoreCase(login);

            if (existente.isPresent()) {
                if (existente.get() instanceof Professor) {
                    Professor professorExistente
                            = (Professor) existente.get();

                    professorExistente.setSenhaHash(
                            encoder.encode(senha)
                    );

                    professores.save(professorExistente);

                    System.out.println(
                            "Senha do professor de demonstracao atualizada."
                    );

                    return;
                }

                throw new IllegalStateException(
                        "Login de demonstracao ja pertence a outro tipo de usuario."
                );
            }

            String cpf = "11144477735";

            if (professores.existsByCpf(cpf)) {
                throw new IllegalStateException(
                        "CPF do professor de demonstracao ja existe."
                );
            }

            // Procura a instituicao participante
            Instituicao instituicao = instituicoes.findAll()
                    .stream()
                    .filter(i -> i.getNome()
                            .equalsIgnoreCase("PUC Minas"))
                    .findFirst()
                    .orElseGet(() -> {
                        Instituicao nova = new Instituicao();
                        nova.setNome("PUC Minas");
                        return instituicoes.save(nova);
                    });

            Professor professor = new Professor();

            professor.setNome("Professor Demonstracao");
            professor.setEmail("professor.demo@example.com");
            professor.setLogin(login);
            professor.setSenhaHash(encoder.encode(senha));
            professor.setCpf(cpf);
            professor.setDepartamento("Engenharia de Software");
            professor.setInstituicao(instituicao);

            professores.save(professor);

            System.out.println(
                    "Professor de demonstracao cadastrado."
            );
        };
    }
}
