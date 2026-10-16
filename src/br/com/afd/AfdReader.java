package br.com.afd;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lê um arquivo AFD linha a linha, interpretando-o conforme a portaria e aplicando os filtros. */
public final class AfdReader {

    private static final int MAX_MENSAGENS_ERRO = 20;

    private AfdReader() {
    }

    /**
     * Tenta UTF-8 e, se o arquivo tiver bytes que não são UTF-8 válido (comum em AFDs antigos com
     * acentos), lê novamente como ISO-8859-1.
     */
    public static ResultadoLeitura ler(Path arquivo, Portaria portaria, FiltroAfd filtro) throws IOException {
        try {
            return ler(arquivo, portaria, filtro, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            return ler(arquivo, portaria, filtro, StandardCharsets.ISO_8859_1);
        }
    }

    private static ResultadoLeitura ler(Path arquivo, Portaria portaria, FiltroAfd filtro, Charset charset)
            throws IOException {
        AfdParser parser = portaria.getParser();
        List<RegistroAfd> aceitos = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        int linhas = 0;
        int lidos = 0;
        int ignoradas = 0;
        int invalidas = 0;

        try (BufferedReader leitor = Files.newBufferedReader(arquivo, charset)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                linhas++;
                if (linhas == 1 && !linha.isEmpty() && linha.charAt(0) == '﻿') {
                    linha = linha.substring(1);
                }
                try {
                    RegistroAfd registro = parser.parse(linha, linhas);
                    if (registro == null) {
                        ignoradas++;
                        continue;
                    }
                    lidos++;
                    if (filtro.aceita(registro)) {
                        aceitos.add(registro);
                    }
                } catch (AfdFormatException e) {
                    invalidas++;
                    if (erros.size() < MAX_MENSAGENS_ERRO) {
                        erros.add("Linha " + linhas + ": " + e.getMessage());
                    }
                }
            }
        }
        return new ResultadoLeitura(aceitos, linhas, lidos, ignoradas, invalidas, erros, charset);
    }
}
