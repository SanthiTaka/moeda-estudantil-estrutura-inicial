
package br.edu.pucminas.moedaestudantil.model.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentoValidatorTest {

    @Test
    void deveAceitarCpfValido() {
        assertTrue(
            DocumentoValidator.cpfValido("52998224725")
        );
    }

    @Test
    void deveRejeitarCpfInvalido() {
        assertFalse(
            DocumentoValidator.cpfValido("12345678901")
        );

        assertFalse(
            DocumentoValidator.cpfValido("11111111111")
        );
    }

    @Test
    void deveAceitarCnpjNumericoValido() {
        assertTrue(
            DocumentoValidator.cnpjValido("11222333000181")
        );
    }

    @Test
    void deveAceitarCnpjAlfanumericoValido() {
        assertTrue(
            DocumentoValidator.cnpjValido("00000000E08G12")
        );
    }

    @Test
    void deveRejeitarCnpjInvalido() {
        assertFalse(
            DocumentoValidator.cnpjValido("11222333000199")
        );

        assertFalse(
            DocumentoValidator.cnpjValido("00000000000000")
        );
    }
}
