
package br.edu.pucminas.moedaestudantil.model.validation;

public final class DocumentoValidator {

    private DocumentoValidator() {}

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }

        int primeiro = digito(soma);

        soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }

        soma += primeiro * 2;

        int segundo = digito(soma);

        return cpf.charAt(9) - '0' == primeiro
            && cpf.charAt(10) - '0' == segundo;
    }

    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null ||
            !cnpj.matches("[A-Z0-9]{12}[0-9]{2}")) {
            return false;
        }

        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int[] pesos1 = {
            5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int[] pesos2 = {
            6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        String base = cnpj.substring(0, 12);

        int primeiro = calcularCnpj(base, pesos1);
        int segundo = calcularCnpj(base + primeiro, pesos2);

        return cnpj.charAt(12) - '0' == primeiro
            && cnpj.charAt(13) - '0' == segundo;
    }

    private static int calcularCnpj(
            String valor, int[] pesos) {

        int soma = 0;

        for (int i = 0; i < valor.length(); i++) {
            // ASCII - 48: funciona com numeros e letras
            int numero = valor.charAt(i) - 48;
            soma += numero * pesos[i];
        }

        return digito(soma);
    }

    private static int digito(int soma) {
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
