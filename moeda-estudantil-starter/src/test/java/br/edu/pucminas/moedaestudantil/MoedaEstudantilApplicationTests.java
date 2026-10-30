
package br.edu.pucminas.moedaestudantil;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "APP_DEV_PASSWORD=SenhaTesteIntegracao123!",
    "spring.datasource.url=jdbc:h2:mem:moeda_contexto;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MoedaEstudantilApplicationTests {

    @Test
    void contextoInicializa() {
        // Verifica se o Spring Boot inicia corretamente
    }
}
