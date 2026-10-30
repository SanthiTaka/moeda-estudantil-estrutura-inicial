
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.dto.response.PerfilResponse;
import br.edu.pucminas.moedaestudantil.model.entity.*;
import br.edu.pucminas.moedaestudantil.model.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService
        implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public AutenticacaoService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        Usuario usuario = localizar(login);

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(obterPerfil(usuario))
                .build();
    }

    @Transactional(readOnly = true)
    public PerfilResponse identificar(String login) {

        Usuario usuario = localizar(login);

        return new PerfilResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                obterPerfil(usuario)
        );
    }

    private Usuario localizar(String login) {
        return usuarios.findByLoginIgnoreCase(login)
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "Usuario nao encontrado."
                    )
                );
    }

    private String obterPerfil(Usuario usuario) {

        if (usuario instanceof Aluno) {
            return "ALUNO";
        }

        if (usuario instanceof Professor) {
            return "PROFESSOR";
        }

        if (usuario instanceof EmpresaParceira) {
            return "EMPRESA";
        }

        throw new IllegalStateException(
            "Perfil de usuario desconhecido."
        );
    }
}
