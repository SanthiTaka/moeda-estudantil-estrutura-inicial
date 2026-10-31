
package br.edu.pucminas.moedaestudantil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.spa())

            .authorizeHttpRequests(auth -> auth

                // Interface web
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()

                // Recursos publicos
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/csrf",
                    "/api/instituicoes"
                ).permitAll()

                // Cadastro publico de alunos e empresas
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/alunos",
                    "/api/empresas"
                ).permitAll()

                // Perfil do usuario autenticado
                .requestMatchers("/api/auth/me")
                    .authenticated()

                // Cadastros protegidos por perfil
                .requestMatchers("/api/alunos/**")
                    .hasRole("ALUNO")

                .requestMatchers(
                    HttpMethod.GET, "/api/alunos")
                    .hasRole("ALUNO")

                .requestMatchers("/api/empresas/**")
                    .hasRole("EMPRESA")

                .requestMatchers(
                    HttpMethod.GET, "/api/empresas")
                    .hasRole("EMPRESA")

                // Demais operacoes serao liberadas
                // conforme os modulos forem implementados
                
// Consulta dos proprios cupons
.requestMatchers(
    HttpMethod.GET, "/api/resgates/me"
).hasRole("ALUNO")

// Criacao de um resgate
.requestMatchers(
    HttpMethod.POST, "/api/resgates"
).hasRole("ALUNO")

                
.requestMatchers(
    HttpMethod.GET, "/api/extratos/me"
).hasAnyRole("ALUNO", "PROFESSOR")

.requestMatchers(
    HttpMethod.GET, "/api/moedas/alunos"
).hasRole("PROFESSOR")


// Empresa consulta somente suas vantagens
.requestMatchers(
    HttpMethod.GET, "/api/vantagens/minhas"
).hasRole("EMPRESA")

// Alunos e empresas consultam o catalogo
.requestMatchers(
    HttpMethod.GET,
    "/api/vantagens",
    "/api/vantagens/*"
).hasAnyRole("ALUNO", "EMPRESA")

// Somente empresas gerenciam vantagens
.requestMatchers(
    HttpMethod.POST, "/api/vantagens"
).hasRole("EMPRESA")

.requestMatchers(
    HttpMethod.PUT, "/api/vantagens/*"
).hasRole("EMPRESA")

.requestMatchers(
    HttpMethod.DELETE, "/api/vantagens/*"
).hasRole("EMPRESA")


.requestMatchers(
    HttpMethod.POST, "/api/moedas/enviar"
).hasRole("PROFESSOR")

                .anyRequest().denyAll()
            )

            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("login")
                .passwordParameter("senha")
                .successHandler((req, res, auth) ->
                    res.setStatus(HttpStatus.NO_CONTENT.value())
                )
                .failureHandler((req, res, ex) ->
                    res.sendError(HttpStatus.UNAUTHORIZED.value())
                )
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) ->
                    res.setStatus(HttpStatus.NO_CONTENT.value())
                )
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(
                        HttpStatus.UNAUTHORIZED
                    )
                )
            )

            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
