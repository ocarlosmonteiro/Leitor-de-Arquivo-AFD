package br.com.afd;

/** Utilitários para ler campos de largura fixa usando a numeração (base 1) da documentação das portarias. */
final class Campos {

    private Campos() {
    }

    /**
     * Retorna os caracteres das posições {@code inicio} a {@code fim}, ambas inclusivas e começando em 1.
     * Se a linha terminar antes, devolve apenas o que existir.
     */
    static String campo(String linha, int inicio, int fim) {
        if (inicio > linha.length()) {
            return "";
        }
        return linha.substring(inicio - 1, Math.min(fim, linha.length()));
    }

    /**
     * Remove o zero que o relógio insere à esquerda quando o documento tem 11 dígitos
     * (campo de 12 posições). Se o primeiro dígito for outro, ele é mantido.
     */
    static String normalizarDocumento(String campo) {
        String c = campo.trim();
        if (c.length() > 11 && c.charAt(0) == '0') {
            c = c.substring(1);
        }
        return c;
    }

    static String validarDocumento(String documento, String rotulo) throws AfdFormatException {
        if (!documento.matches("\\d{11,12}")) {
            throw new AfdFormatException(rotulo + " inválido: \"" + documento + "\"");
        }
        return documento;
    }

    static void exigirTamanho(String linha, int minimo) throws AfdFormatException {
        if (linha.length() < minimo) {
            throw new AfdFormatException("linha com " + linha.length()
                    + " caracteres; esperado pelo menos " + minimo);
        }
    }
}
