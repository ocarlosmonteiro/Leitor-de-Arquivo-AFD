package br.com.afd;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Testes simples, sem dependências. Execute a partir da pasta do projeto (precisa de ./exemplos):
 * {@code java -cp build/classes:build/test-classes br.com.afd.AfdTestes}
 */
public final class AfdTestes {

    private static int total;
    private static int falhas;

    private AfdTestes() {
    }

    public static void main(String[] args) throws Exception {
        parser1510();
        parser671();
        filtro();
        arquivo1510();
        arquivo671();
        System.out.println(total + " verificações, " + falhas + " falha(s)");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ parsers

    private static void parser1510() throws Exception {
        AfdParser p = new Afd1510Parser();

        RegistroAfd m = p.parse("000000004" + "3" + "02102026" + "0745" + "012345678919", 4);
        confere("1510 marcação: evento", TipoEvento.MARCACAO, m.getEvento());
        confere("1510 marcação: NSR", "000000004", m.getNsr());
        confere("1510 marcação: data/hora", LocalDateTime.of(2026, 10, 2, 7, 45), m.getDataHora());
        confere("1510 marcação: PIS sem o 0 à esquerda", "12345678919", m.getDocumento());
        confere("1510 marcação: linha", 4, m.getNumeroLinha());

        RegistroAfd comEspacos = p.parse("000000004" + "3" + "02102026" + "0745" + "012345678919" + "   ", 4);
        confere("1510 marcação: espaços no fim da linha", "12345678919", comEspacos.getDocumento());

        RegistroAfd outro = p.parse("000000010" + "3" + "02102026" + "0745" + "712345678919", 10);
        confere("1510 marcação: dígito diferente de 0 é mantido", "712345678919", outro.getDocumento());

        RegistroAfd zeroNoPis = p.parse("000000011" + "3" + "02102026" + "0745" + "001234567890", 11);
        confere("1510 marcação: PIS que começa com 0 mantém o seu zero", "01234567890", zeroNoPis.getDocumento());

        String nome = "JOSE ANTONIO";
        RegistroAfd i = p.parse("000000003" + "5" + "01102026" + "0802" + "I" + "012345678919" + pad(nome, 52), 3);
        confere("1510 cadastro I: evento", TipoEvento.INCLUSAO, i.getEvento());
        confere("1510 cadastro I: PIS", "12345678919", i.getDocumento());
        confere("1510 cadastro I: nome", nome, i.getNome());
        confere("1510 cadastro I: data/hora", LocalDateTime.of(2026, 10, 1, 8, 2), i.getDataHora());
        confere("1510 cadastro A", TipoEvento.ALTERACAO,
                p.parse("000000003" + "5" + "01102026" + "0802" + "A" + "012345678919" + pad(nome, 52), 3).getEvento());
        confere("1510 cadastro E", TipoEvento.EXCLUSAO,
                p.parse("000000003" + "5" + "01102026" + "0802" + "E" + "012345678919" + pad(nome, 52), 3).getEvento());

        confere("1510 tipo 1 é ignorado", null, p.parse("000000000" + "1" + "qualquer coisa", 1));
        confere("1510 tipo 9 é ignorado", null, p.parse("999999999000000001000000006000000000000000059", 1));
        confere("1510 linha vazia é ignorada", null, p.parse("", 1));

        esperaErro("1510 operação desconhecida", p,
                "000000003" + "5" + "01102026" + "0802" + "X" + "012345678919" + pad(nome, 52));
        esperaErro("1510 data inexistente", p, "000000004" + "3" + "31022026" + "0745" + "012345678919");
        esperaErro("1510 hora inexistente", p, "000000004" + "3" + "02102026" + "2575" + "012345678919");
        esperaErro("1510 marcação curta", p, "000000004" + "3" + "02102026" + "0745");
        esperaErro("1510 PIS com letras", p, "000000004" + "3" + "02102026" + "0745" + "0123456789AB");
    }

    private static void parser671() throws Exception {
        AfdParser p = new Afd671Parser();
        String dt = "2026-10-02T07:45:00-0300";

        RegistroAfd m = p.parse("000000002" + "3" + dt + "052998224725" + "ABCD", 2);
        confere("671 marcação: evento", TipoEvento.MARCACAO, m.getEvento());
        confere("671 marcação: data/hora", LocalDateTime.of(2026, 10, 2, 7, 45), m.getDataHora());
        confere("671 marcação: CPF", "52998224725", m.getDocumento());

        RegistroAfd zeros = p.parse("000000002" + "3" + dt + "000000000191" + "ABCD", 2);
        confere("671 marcação: CPF com zeros à esquerda", "00000000191", zeros.getDocumento());

        RegistroAfd c = p.parse("000000005" + "5" + dt + "I" + "052998224725" + pad("MARIA DA SILVA", 52)
                + "0000" + "ABCD", 5);
        confere("671 cadastro: evento", TipoEvento.INCLUSAO, c.getEvento());
        confere("671 cadastro: CPF", "52998224725", c.getDocumento());
        confere("671 cadastro: nome", "MARIA DA SILVA", c.getNome());

        RegistroAfd rep7 = p.parse("000000006" + "7" + dt + "052998224725" + "resto do registro", 6);
        confere("671 tipo 7: marcação", TipoEvento.MARCACAO, rep7.getEvento());
        confere("671 tipo 7: tipo do registro", '7', rep7.getTipoRegistro());

        confere("671 tipo 1 é ignorado", null, p.parse("000000000" + "1" + "xxxxxxxxxxxx", 1));
        esperaErro("671 data inválida", p, "000000002" + "3" + "2026-13-02T07:45:00-0300" + "052998224725" + "ABCD");
        esperaErro("671 marcação curta", p, "000000002" + "3" + dt);
        esperaErro("671 operação desconhecida", p, "000000005" + "5" + dt + "Z" + "052998224725"
                + pad("MARIA", 52) + "0000" + "ABCD");
    }

    // ------------------------------------------------------------------ filtro

    private static void filtro() throws Exception {
        AfdParser p = new Afd1510Parser();
        RegistroAfd r = p.parse("000000004" + "3" + "02102026" + "0745" + "001234567890", 4);

        confere("filtro vazio aceita", true, FiltroAfd.SEM_FILTRO.aceita(r));
        confere("filtro por documento exato", true, new FiltroAfd("01234567890", null, null, null).aceita(r));
        confere("filtro por documento sem zeros à esquerda", true, new FiltroAfd("1234567890", null, null, null).aceita(r));
        confere("filtro por documento mascarado", true, new FiltroAfd("012.34567.89-0", null, null, null).aceita(r));
        confere("filtro por documento diferente", false, new FiltroAfd("12345678919", null, null, null).aceita(r));
        confere("filtro por evento", false, new FiltroAfd(null, null, null, TipoEvento.INCLUSAO).aceita(r));
        confere("data inicial igual ao dia", true,
                new FiltroAfd(null, LocalDate.of(2026, 10, 2), null, null).aceita(r));
        confere("data final anterior ao dia", false,
                new FiltroAfd(null, null, LocalDate.of(2026, 10, 1), null).aceita(r));
    }

    // ------------------------------------------------------------------ arquivos de exemplo

    private static void arquivo1510() throws Exception {
        ResultadoLeitura r = AfdReader.ler(Paths.get("exemplos", "afd_1510_exemplo.txt"), Portaria.P1510,
                FiltroAfd.SEM_FILTRO);
        confere("arq1510: total de linhas", 13, r.getTotalLinhas());
        confere("arq1510: registros lidos", 11, r.getRegistrosLidos());
        confere("arq1510: linhas ignoradas (cabeçalho e trailer)", 2, r.getLinhasIgnoradas());
        confere("arq1510: linhas inválidas", 0, r.getLinhasInvalidas());
        confere("arq1510: codificação detectada", StandardCharsets.ISO_8859_1, r.getCharset());
        String nomeJose = r.getRegistros().get(2).getNome();
        confere("arq1510: acentos lidos corretamente", "JOSÉ ANTÔNIO CONCEIÇÃO", nomeJose);

        confere("arq1510: filtro por PIS da Maria", 4, contar("12345678919", null, null, null, Portaria.P1510));
        confere("arq1510: filtro por PIS do José (começa com 0)", 3,
                contar("01234567890", null, null, null, Portaria.P1510));
        confere("arq1510: filtro por PIS do José digitado sem o zero", 3,
                contar("1234567890", null, null, null, Portaria.P1510));
        confere("arq1510: filtro por período", 3,
                contar(null, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9), null, Portaria.P1510));
        confere("arq1510: só marcações", 6, contar(null, null, null, TipoEvento.MARCACAO, Portaria.P1510));
        confere("arq1510: marcações da Maria", 3,
                contar("12345678919", null, null, TipoEvento.MARCACAO, Portaria.P1510));
        confere("arq1510: exclusões", 1, contar(null, null, null, TipoEvento.EXCLUSAO, Portaria.P1510));

        ResultadoLeitura errada = AfdReader.ler(Paths.get("exemplos", "afd_671_exemplo.txt"), Portaria.P1510,
                FiltroAfd.SEM_FILTRO);
        confere("arq671 lido como 1510: nada deve ser aceito como registro válido", 0, errada.getRegistros().size());
    }

    private static void arquivo671() throws Exception {
        ResultadoLeitura r = AfdReader.ler(Paths.get("exemplos", "afd_671_exemplo.txt"), Portaria.P671,
                FiltroAfd.SEM_FILTRO);
        confere("arq671: total de linhas", 13, r.getTotalLinhas());
        confere("arq671: registros lidos", 11, r.getRegistrosLidos());
        confere("arq671: linhas ignoradas", 2, r.getLinhasIgnoradas());
        confere("arq671: linhas inválidas", 0, r.getLinhasInvalidas());
        confere("arq671: codificação detectada", StandardCharsets.UTF_8, r.getCharset());
        confere("arq671: acentos lidos corretamente", "JOÃO PEREIRA DE ALMEIDA", r.getRegistros().get(1).getNome());
        confere("arq671: CPF do José com zeros à esquerda", "00000000191", r.getRegistros().get(2).getDocumento());

        confere("arq671: filtro por CPF da Maria", 4, contar("52998224725", null, null, null, Portaria.P671));
        confere("arq671: filtro por CPF mascarado", 4, contar("529.982.247-25", null, null, null, Portaria.P671));
        confere("arq671: filtro por CPF do José", 3, contar("191", null, null, null, Portaria.P671));
        confere("arq671: filtro por período", 3,
                contar(null, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9), null, Portaria.P671));
        confere("arq671: alterações", 1, contar(null, null, null, TipoEvento.ALTERACAO, Portaria.P671));
    }

    private static int contar(String doc, LocalDate ini, LocalDate fim, TipoEvento ev, Portaria portaria)
            throws Exception {
        String arquivo = portaria == Portaria.P1510 ? "afd_1510_exemplo.txt" : "afd_671_exemplo.txt";
        return AfdReader.ler(Paths.get("exemplos", arquivo), portaria, new FiltroAfd(doc, ini, fim, ev))
                .getRegistros().size();
    }

    // ------------------------------------------------------------------ apoio

    private static String pad(String texto, int tamanho) {
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < tamanho) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static void confere(String nome, Object esperado, Object obtido) {
        total++;
        boolean ok = esperado == null ? obtido == null : esperado.equals(obtido);
        if (!ok) {
            falhas++;
            System.out.println("FALHOU: " + nome + " -> esperado [" + esperado + "], obtido [" + obtido + "]");
        }
    }

    private static void esperaErro(String nome, AfdParser parser, String linha) {
        total++;
        try {
            parser.parse(linha, 1);
            falhas++;
            System.out.println("FALHOU: " + nome + " (deveria lançar AfdFormatException)");
        } catch (AfdFormatException e) {
            // esperado
        }
    }
}
