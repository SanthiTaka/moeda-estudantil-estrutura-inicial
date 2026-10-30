
package br.edu.pucminas.moedaestudantil.model.service;

import br.edu.pucminas.moedaestudantil.exception.ConflitoCadastroException;
import br.edu.pucminas.moedaestudantil.model.entity.Usuario;
import br.edu.pucminas.moedaestudantil.model.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ValidacaoCadastro {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public ValidacaoCadastro(
            UsuarioRepository usuarios,
            PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public void conferirUnicidade(
            String email, String login, Usuario atual) {

        if ((atual == null ||
                !atual.getEmail().equalsIgnoreCase(email))
                && usuarios.existsByEmail(email)) {
            throw new ConflitoCadastroException(
                    "Este e-mail ja esta cadastrado.");
        }

        if ((atual == null ||
                !atual.getLogin().equalsIgnoreCase(login))
                && usuarios.existsByLogin(login)) {
            throw new ConflitoCadastroException(
                    "Este login ja esta cadastrado.");
        }
    }

    public void atualizarSenha(
            Usuario usuario, String senha, boolean novo) {

        if (senha == null || senha.isBlank()) {
            if (novo) {
                throw new IllegalArgumentException(
                        "Senha e obrigatoria no cadastro.");
            }
            return;
        }

        if (senha.length() < 8 || senha.length() > 72) {
            throw new IllegalArgumentException(
                    "A senha deve ter entre 8 e 72 caracteres.");
        }

        usuario.setSenhaHash(encoder.encode(senha));
    }
}
