
package br.edu.pucminas.moedaestudantil.exception;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(
            MethodArgumentNotValidException e) {

        var erros = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        erro -> erro.getField(),
                        erro -> erro.getDefaultMessage() == null
                                ? "Invalido"
                                : erro.getDefaultMessage(),
                        (a, b) -> a
                ));

        return ResponseEntity.badRequest().body(
                Map.of(
                        "mensagem", "Verifique os campos informados.",
                        "erros", erros
                ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(
            NoSuchElementException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(ConflitoCadastroException.class)
    public ResponseEntity<Map<String, String>> conflito(
            ConflitoCadastroException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumento(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest()
                .body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridade(
            DataIntegrityViolationException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "mensagem",
                        "Operacao impedida por dados vinculados ou valor ja cadastrado."
                ));
    }
}
