# 🪙 Sistema de Moeda Estudantil — Estrutura Inicial

Projeto acadêmico de Engenharia de Software, organizado em **MVC com Spring Boot**.

> **Estado atual:** apenas estrutura inicial. As entidades JPA e os repositórios estão definidos, mas os Controllers e Services **não implementam operações de negócio**, a autenticação personalizada não existe, não há envio de e-mail, crédito semestral ou telas funcionais. Os DTOs também precisarão de validações.

## Tecnologias
- Java 21
- Spring Boot 4.1.2
- Spring MVC
- Spring Data JPA / Hibernate (ORM)
- Spring Security (dependência adicionada; configuração personalizada pendente)
- H2 em arquivo (banco de desenvolvimento)
- Maven

## MVC
- **View**: `src/main/resources/static` — HTML, CSS, JavaScript.
- **Controller**: `src/main/java/br/edu/pucminas/moedaestudantil/controller` — endpoints REST.
- **Model**: `model/entity`, `model/service`, `model/repository` — entidades, regras e persistência.
- **Infraestrutura**: agendamento e e-mail (contratos iniciais).

## Modelo ER
Entidades: Usuario, Aluno, Professor, EmpresaParceira, Instituicao, Conta, Transacao, Vantagem, Resgate e Notificacao. `Usuario` usa herança JPA `JOINED` para os perfis.

**Observação:** o controle de concorrência de `Conta` inclui `versao` (`@Version`), campo técnico não mostrado no ER simplificado. Para registrar créditos semestrais sem duplicação e vincular transferências será necessário complementar o modelo e atualizar os diagramas antes da entrega definitiva.

## Rodar
Com JDK 21 e Maven instalados:

```bash
mvn spring-boot:run
```

O Spring Security está incluído e inicialmente protege as rotas com o comportamento padrão. Não há ainda login funcional por perfil. O banco H2 local será criado na pasta `data/`. Não faça commit de arquivos do banco nem de credenciais.

## Ordem recomendada
1. Validar entidades, relacionamentos e criação das tabelas.
2. Implementar DTOs, Services e Controllers de Aluno e EmpresaParceira.
3. Configurar Security e autenticação/autorização por perfil.
4. Professores, contas, créditos semestrais e transações (com atomicidade).
5. Vantagens, resgates e códigos únicos.
6. Notificações por e-mail após commit e testes.
7. Telas e testes automatizados.
