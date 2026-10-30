
"use strict";

const $ = id => document.getElementById(id);

const mostrar = (id, visivel) =>
    $(id).classList.toggle("hidden", !visivel);

let sessao = null;
let modoEdicao = false;

// Mensagens
function aviso(mensagem, erro = false) {
    $("feedback").textContent = mensagem;
    $("feedback").classList.toggle("error", erro);
    mostrar("feedback", true);
}

// Protecao CSRF
function tokenCsrf() {
    const item = document.cookie.split("; ")
        .find(c => c.startsWith("XSRF-TOKEN="));

    if (!item) {
        throw new Error(
            "Token de seguranca ausente. Atualize a pagina."
        );
    }

    return decodeURIComponent(
        item.substring("XSRF-TOKEN=".length)
    );
}

async function prepararCsrf() {
    const resposta = await fetch("/api/auth/csrf", {
        credentials: "same-origin",
        cache: "no-store"
    });

    if (!resposta.ok) {
        throw new Error("Falha ao preparar a seguranca.");
    }

    tokenCsrf();
}

// Requisicoes HTTP
async function api(caminho, opcoes = {}) {
    const metodo = (opcoes.method || "GET").toUpperCase();

    const headers = {
        Accept: "application/json",
        ...opcoes.headers
    };

    if (opcoes.body && typeof opcoes.body === "string") {
        headers["Content-Type"] = "application/json";
    }

    if (!["GET", "HEAD", "OPTIONS"].includes(metodo)) {
        headers["X-XSRF-TOKEN"] = tokenCsrf();
    }

    const resposta = await fetch(caminho, {
        ...opcoes,
        method: metodo,
        headers,
        credentials: "same-origin",
        cache: "no-store"
    });

    if (resposta.status === 204) return null;

    const texto = await resposta.text();
    let dados = null;

    try {
        dados = texto ? JSON.parse(texto) : null;
    } catch {
        // Resposta sem JSON
    }

    if (!resposta.ok) {
        const detalhes = dados?.erros
            ? Object.entries(dados.erros)
                .map(([c, v]) => `${c}: ${v}`)
                .join(" | ")
            : dados?.mensagem;

        throw new Error(
            detalhes || `Erro HTTP ${resposta.status}`
        );
    }

    return dados;
}

// Painel do usuario
function mostrarPainel() {
    mostrar("acesso", false);
    mostrar("cadastro", false);
    mostrar("painel", true);

    $("boas-vindas").textContent =
        `Bem-vindo(a), ${sessao.nome}!`;

    $("resumo-perfil").textContent =
        `Login: ${sessao.login} | Perfil: ${sessao.perfil}`;

    mostrar(
        "editar-conta",
        sessao.perfil !== "PROFESSOR"
    );

    mostrar(
        "excluir-conta",
        sessao.perfil !== "PROFESSOR"
    );
}

async function carregarPerfil() {
    sessao = await api("/api/auth/me");
    mostrarPainel();
}

// Login real
async function entrar(login, senha) {
    await prepararCsrf();

    const resposta = await fetch("/api/auth/login", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type":
                "application/x-www-form-urlencoded",
            "X-XSRF-TOKEN": tokenCsrf()
        },
        body: new URLSearchParams({ login, senha })
    });

    if (!resposta.ok) {
        throw new Error("Login ou senha incorretos.");
    }

    // Renova o CSRF apos autenticar
    await prepararCsrf();
    await carregarPerfil();
}

// Logout
async function sair() {
    try {
        await api("/api/auth/logout", {
            method: "POST"
        });

        sessao = null;

        $("form-login").reset();

        mostrar("painel", false);
        mostrar("cadastro", false);
        mostrar("acesso", true);

        await prepararCsrf();

        aviso("Voce saiu da conta.");
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Instituicoes
async function preencherInstituicoes() {
    const instituicoes = await api("/api/instituicoes");

    const select = $("aluno-instituicao");

    select.replaceChildren(
        new Option("Selecione...", "")
    );

    for (const instituicao of instituicoes) {
        select.add(
            new Option(
                instituicao.nome,
                String(instituicao.id)
            )
        );
    }
}

// Cadastro e edicao
function abrirCadastro(tipo, editar = false) {
    modoEdicao = editar;

    $("form-aluno").reset();
    $("form-empresa").reset();

    mostrar("acesso", false);
    mostrar("painel", false);
    mostrar("cadastro", true);

    mostrar("form-aluno", tipo === "alunos");
    mostrar("form-empresa", tipo === "empresas");

    $("cadastro-titulo").textContent = editar
        ? "Editar meus dados"
        : tipo === "alunos"
            ? "Cadastro de aluno"
            : "Cadastro de empresa";

    const prefixo = tipo === "alunos"
        ? "aluno" : "empresa";

    $(`${prefixo}-senha`).required = !editar;
    $(`${prefixo}-login`).readOnly = editar;

    $("dica-senha").textContent = editar
        ? "Deixe a senha em branco para mante-la."
        : "A senha precisa ter ao menos 8 caracteres.";
}

// Buscar meus dados para edicao
async function editarConta() {
    const tipo = sessao.perfil === "ALUNO"
        ? "alunos" : "empresas";

    try {
        const dados = await api(
            `/api/${tipo}/${sessao.id}`
        );

        abrirCadastro(tipo, true);

        const prefixo = tipo === "alunos"
            ? "aluno" : "empresa";

        const campos = [
            "nome", "email", "login",
            ...(tipo === "alunos"
                ? ["cpf", "rg", "endereco", "curso"]
                : ["cnpj"])
        ];

        for (const campo of campos) {
            $(`${prefixo}-${campo}`).value =
                dados[campo] ?? "";
        }

        if (tipo === "alunos") {
            $("aluno-instituicao").value =
                String(dados.instituicaoId);
        }
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Cadastrar ou atualizar
async function salvarCadastro(evento, tipo) {
    evento.preventDefault();

    const prefixo = tipo === "alunos"
        ? "aluno" : "empresa";

    const form = $(`form-${prefixo}`);

    const dados = Object.fromEntries(
        new FormData(form).entries()
    );

    const senhaDigitada = dados.senha;

    dados.senha = dados.senha || null;

    if (tipo === "alunos") {
        dados.cpf = dados.cpf.replace(/\D/g, "");
        dados.instituicaoId = Number(dados.instituicaoId);
    } else {
        dados.cnpj = dados.cnpj
            .replace(/[^A-Za-z0-9]/g, "")
            .toUpperCase();
    }

    const caminho = `/api/${tipo}${
        modoEdicao ? `/${sessao.id}` : ""
    }`;

    try {
        await api(caminho, {
            method: modoEdicao ? "PUT" : "POST",
            body: JSON.stringify(dados)
        });

        if (modoEdicao) {
            await carregarPerfil();
            aviso("Cadastro atualizado.");
        } else {
            await entrar(dados.login, senhaDigitada);
            aviso("Conta criada e login realizado!");
        }

        form.reset();
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Excluir minha conta
async function excluirConta() {
    if (!confirm(
        "Deseja excluir seu cadastro permanentemente?"
    )) return;

    const tipo = sessao.perfil === "ALUNO"
        ? "alunos" : "empresas";

    try {
        await api(`/api/${tipo}/${sessao.id}`, {
            method: "DELETE"
        });

        await sair();
        aviso("Cadastro excluido.");
    } catch (erro) {
        aviso(erro.message, true);
    }
}

// Eventos
$("form-login").addEventListener(
    "submit", async evento => {
        evento.preventDefault();

        try {
            await entrar(
                $("login-usuario").value.trim(),
                $("login-senha").value
            );

            $("login-senha").value = "";

            aviso("Login realizado!");
        } catch (erro) {
            aviso(erro.message, true);
        }
    }
);

$("abrir-aluno").addEventListener(
    "click", () => abrirCadastro("alunos")
);

$("abrir-empresa").addEventListener(
    "click", () => abrirCadastro("empresas")
);

$("voltar").addEventListener("click", () => {
    if (sessao) {
        mostrarPainel();
    } else {
        mostrar("cadastro", false);
        mostrar("acesso", true);
    }
});

$("form-aluno").addEventListener(
    "submit", e => salvarCadastro(e, "alunos")
);

$("form-empresa").addEventListener(
    "submit", e => salvarCadastro(e, "empresas")
);

$("editar-conta").addEventListener(
    "click", editarConta
);

$("excluir-conta").addEventListener(
    "click", excluirConta
);

$("sair").addEventListener("click", sair);

// Inicializacao
(async () => {
    try {
        await prepararCsrf();
        await preencherInstituicoes();

        try {
            await carregarPerfil();
        } catch {
            mostrar("acesso", true);
        }
    } catch (erro) {
        aviso(erro.message, true);
    }
})();
