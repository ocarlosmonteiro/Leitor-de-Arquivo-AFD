package br.com.afd;

/** Formatação de PIS e CPF para exibição. */
final class Documentos {

    private Documentos() {
    }

    /** PIS: 000.00000.00-0; CPF: 000.000.000-00. Valores que não têm 11 dígitos são devolvidos sem máscara. */
    static String formatar(String documento, Portaria portaria) {
        if (documento.length() != 11) {
            return documento;
        }
        if (portaria == Portaria.P671) {
            return documento.substring(0, 3) + "." + documento.substring(3, 6) + "."
                    + documento.substring(6, 9) + "-" + documento.substring(9);
        }
        return documento.substring(0, 3) + "." + documento.substring(3, 8) + "."
                + documento.substring(8, 10) + "-" + documento.substring(10);
    }
}
