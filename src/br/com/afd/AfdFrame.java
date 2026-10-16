package br.com.afd;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.concurrent.ExecutionException;
import java.util.function.IntPredicate;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Janela principal do leitor de AFD. */
public final class AfdFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter ENTRADA_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter EXIBE_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EXIBE_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final JTextField campoCaminho = new JTextField();
    private final JButton botaoSelecionar = new JButton("Selecionar arquivo...");
    private final JRadioButton opcao1510 = new JRadioButton(Portaria.P1510.getDescricao(), true);
    private final JRadioButton opcao671 = new JRadioButton(Portaria.P671.getDescricao());

    private final JLabel rotuloDocumento = new JLabel();
    private final JTextField campoDocumento = new JTextField(14);
    private final JTextField campoDataInicial = new JTextField(9);
    private final JTextField campoDataFinal = new JTextField(9);
    private final JComboBox<Object> comboEvento = new JComboBox<>();

    private final JButton botaoLer = new JButton("Ler arquivo");
    private final JButton botaoLimpar = new JButton("Limpar filtros");
    private final JButton botaoExportar = new JButton("Exportar CSV...");
    private final JProgressBar progresso = new JProgressBar();
    private final JLabel status = new JLabel(
            "Selecione o arquivo AFD, escolha a portaria e clique em \"Ler arquivo\".");

    private final RegistrosTableModel modelo = new RegistrosTableModel();
    private final JTable tabela = new JTable(modelo);

    public AfdFrame() {
        super("Leitor de AFD - Portarias 1510 e 671");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        configurarCampos();
        montarTela();
        configurarTabela();
        ligarEventos();
        atualizarRotuloDocumento();
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ montagem da tela

    private void configurarCampos() {
        ((AbstractDocument) campoDocumento.getDocument()).setDocumentFilter(new FiltroTexto(AfdFrame::ehDigito, 14));
        IntPredicate dataValida = c -> ehDigito(c) || c == '/';
        ((AbstractDocument) campoDataInicial.getDocument()).setDocumentFilter(new FiltroTexto(dataValida, 10));
        ((AbstractDocument) campoDataFinal.getDocument()).setDocumentFilter(new FiltroTexto(dataValida, 10));
        campoDataInicial.setToolTipText("Formato dd/mm/aaaa");
        campoDataFinal.setToolTipText("Formato dd/mm/aaaa");

        comboEvento.addItem("Todos");
        for (TipoEvento evento : TipoEvento.values()) {
            comboEvento.addItem(evento);
        }

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(opcao1510);
        grupo.add(opcao671);

        progresso.setIndeterminate(true);
        progresso.setVisible(false);
        botaoExportar.setEnabled(false);
    }

    private void montarTela() {
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topo = new JPanel(new BorderLayout(0, 6));
        topo.add(criarPainelArquivo(), BorderLayout.NORTH);
        topo.add(criarPainelFiltros(), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.add(progresso);
        direita.add(botaoExportar);
        JPanel rodape = new JPanel(new BorderLayout(8, 0));
        rodape.add(status, BorderLayout.CENTER);
        rodape.add(direita, BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
    }

    private JPanel criarPainelArquivo() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Arquivo"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        painel.add(botaoSelecionar, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        painel.add(campoCaminho, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        painel.add(new JLabel("Portaria:"), c);
        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        opcoes.add(opcao1510);
        opcoes.add(opcao671);
        c.gridx = 1;
        painel.add(opcoes, c);
        return painel;
    }

    private JPanel criarPainelFiltros() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Filtros (opcionais)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;

        c.gridx = 0;
        painel.add(rotuloDocumento, c);
        c.gridx = 1;
        painel.add(campoDocumento, c);
        c.gridx = 2;
        painel.add(new JLabel("Data inicial:"), c);
        c.gridx = 3;
        painel.add(campoDataInicial, c);
        c.gridx = 4;
        painel.add(new JLabel("Data final:"), c);
        c.gridx = 5;
        painel.add(campoDataFinal, c);
        c.gridx = 6;
        painel.add(new JLabel("Registro:"), c);
        c.gridx = 7;
        painel.add(comboEvento, c);
        c.gridx = 8;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel dica = new JLabel("datas no formato dd/mm/aaaa");
        dica.setEnabled(false);
        painel.add(dica, c);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.add(botaoLer);
        acoes.add(botaoLimpar);
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 9;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        painel.add(acoes, c);
        return painel;
    }

    private void configurarTabela() {
        tabela.setAutoCreateRowSorter(true);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tabela.setDefaultRenderer(LocalDate.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object valor) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setText(valor == null ? "" : EXIBE_DATA.format((LocalDate) valor));
            }
        });
        tabela.setDefaultRenderer(LocalTime.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object valor) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setText(valor == null ? "" : EXIBE_HORA.format((LocalTime) valor));
            }
        });
        ajustarColunas();
    }

    private void ajustarColunas() {
        int[] larguras = {70, 100, 50, 90, 90, 60, 130, 400};
        TableColumnModel colunas = tabela.getColumnModel();
        for (int i = 0; i < larguras.length && i < colunas.getColumnCount(); i++) {
            colunas.getColumn(i).setPreferredWidth(larguras[i]);
        }
    }

    private void ligarEventos() {
        botaoSelecionar.addActionListener(e -> selecionarArquivo());
        botaoLer.addActionListener(e -> lerArquivo());
        botaoLimpar.addActionListener(e -> limparFiltros());
        botaoExportar.addActionListener(e -> exportarCsv());
        opcao1510.addActionListener(e -> atualizarRotuloDocumento());
        opcao671.addActionListener(e -> atualizarRotuloDocumento());
        getRootPane().setDefaultButton(botaoLer);
    }

    // ------------------------------------------------------------------ ações

    private Portaria portariaSelecionada() {
        return opcao671.isSelected() ? Portaria.P671 : Portaria.P1510;
    }

    private void atualizarRotuloDocumento() {
        String rotulo = portariaSelecionada().getRotuloDocumento();
        rotuloDocumento.setText(rotulo + " (somente números):");
        campoDocumento.setToolTipText("Informe apenas os números do " + rotulo);
    }

    private void selecionarArquivo() {
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Selecionar arquivo AFD");
        escolha.addChoosableFileFilter(new FileNameExtensionFilter("Arquivos AFD (*.txt, *.afd)", "txt", "afd"));
        String atual = campoCaminho.getText().trim();
        if (!atual.isEmpty()) {
            File pasta = new File(atual).getAbsoluteFile().getParentFile();
            if (pasta != null && pasta.isDirectory()) {
                escolha.setCurrentDirectory(pasta);
            }
        }
        if (escolha.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campoCaminho.setText(escolha.getSelectedFile().getAbsolutePath());
        }
    }

    private void limparFiltros() {
        campoDocumento.setText("");
        campoDataInicial.setText("");
        campoDataFinal.setText("");
        comboEvento.setSelectedIndex(0);
    }

    private void lerArquivo() {
        String caminho = campoCaminho.getText().trim();
        if (caminho.isEmpty()) {
            aviso("Selecione o arquivo AFD.");
            return;
        }
        final Path arquivo;
        try {
            arquivo = Paths.get(caminho);
        } catch (InvalidPathException e) {
            aviso("O caminho informado é inválido.");
            return;
        }
        if (!Files.isRegularFile(arquivo)) {
            aviso("Arquivo não encontrado:\n" + caminho);
            return;
        }
        final FiltroAfd filtro;
        try {
            filtro = montarFiltro();
        } catch (IllegalArgumentException e) {
            aviso(e.getMessage());
            return;
        }

        final Portaria portaria = portariaSelecionada();
        definirOcupado(true);
        new SwingWorker<ResultadoLeitura, Void>() {
            @Override
            protected ResultadoLeitura doInBackground() throws Exception {
                return AfdReader.ler(arquivo, portaria, filtro);
            }

            @Override
            protected void done() {
                definirOcupado(false);
                try {
                    exibir(get(), portaria);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    status.setText("Falha ao ler o arquivo.");
                    erro("Não foi possível ler o arquivo:\n" + causa.getMessage());
                }
            }
        }.execute();
    }

    private FiltroAfd montarFiltro() {
        LocalDate inicial = lerData(campoDataInicial, "Data inicial");
        LocalDate fim = lerData(campoDataFinal, "Data final");
        if (inicial != null && fim != null && inicial.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
        Object selecionado = comboEvento.getSelectedItem();
        TipoEvento evento = selecionado instanceof TipoEvento ? (TipoEvento) selecionado : null;
        return new FiltroAfd(campoDocumento.getText(), inicial, fim, evento);
    }

    private static LocalDate lerData(JTextField campo, String nome) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, ENTRADA_DATA);
        } catch (DateTimeParseException e) {
            campo.requestFocusInWindow();
            throw new IllegalArgumentException(nome + " inválida. Use o formato dd/mm/aaaa.");
        }
    }

    private void exibir(ResultadoLeitura resultado, Portaria portaria) {
        modelo.setDados(resultado.getRegistros(), portaria);
        ajustarColunas();
        botaoExportar.setEnabled(!resultado.getRegistros().isEmpty());
        status.setText(String.format(
                "%d registro(s) exibido(s) de %d lido(s)  |  %d linha(s) ignorada(s)  |  "
                        + "%d linha(s) inválida(s)  |  codificação: %s",
                resultado.getRegistros().size(), resultado.getRegistrosLidos(),
                resultado.getLinhasIgnoradas(), resultado.getLinhasInvalidas(),
                resultado.getCharset().displayName()));

        if (resultado.getLinhasInvalidas() > 0) {
            mostrarLinhasInvalidas(resultado);
        } else if (resultado.getRegistrosLidos() == 0) {
            aviso("Nenhum registro de marcação ou de cadastro foi encontrado.\n"
                    + "Confira se a portaria selecionada é a mesma do arquivo.");
        }
    }

    private void mostrarLinhasInvalidas(ResultadoLeitura resultado) {
        StringBuilder texto = new StringBuilder();
        texto.append(resultado.getLinhasInvalidas()).append(" linha(s) não seguem o layout da ")
                .append(portariaSelecionada().getDescricao()).append(" e foram descartadas");
        if (resultado.getLinhasInvalidas() > resultado.getMensagensErro().size()) {
            texto.append(" (mostrando as primeiras ").append(resultado.getMensagensErro().size()).append(")");
        }
        texto.append(":\n\n");
        for (String mensagem : resultado.getMensagensErro()) {
            texto.append(mensagem).append('\n');
        }
        JTextArea area = new JTextArea(texto.toString(), 12, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Linhas inválidas",
                JOptionPane.WARNING_MESSAGE);
    }

    private void exportarCsv() {
        if (tabela.getRowCount() == 0) {
            aviso("Não há registros para exportar.");
            return;
        }
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Exportar CSV");
        escolha.setSelectedFile(new File("afd_exportado.csv"));
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = escolha.getSelectedFile();
        if (destino.exists() && JOptionPane.showConfirmDialog(this,
                "O arquivo já existe. Deseja substituí-lo?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (BufferedWriter saida = Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8)) {
            saida.write('﻿'); // BOM para o Excel reconhecer UTF-8
            StringBuilder cabecalho = new StringBuilder();
            for (int c = 0; c < modelo.getColumnCount(); c++) {
                if (c > 0) {
                    cabecalho.append(';');
                }
                cabecalho.append(modelo.getColumnName(c));
            }
            saida.write(cabecalho.append("\r\n").toString());
            // segue a ordem atual da tabela (respeita a ordenação escolhida pelo usuário)
            for (int i = 0; i < tabela.getRowCount(); i++) {
                int m = tabela.convertRowIndexToModel(i);
                StringBuilder linha = new StringBuilder();
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    if (c > 0) {
                        linha.append(';');
                    }
                    linha.append(csv(textoDaCelula(modelo.getValueAt(m, c))));
                }
                saida.write(linha.append("\r\n").toString());
            }
            status.setText("Exportado para " + destino.getAbsolutePath());
        } catch (IOException e) {
            erro("Não foi possível gravar o arquivo:\n" + e.getMessage());
        }
    }

    private static String textoDaCelula(Object valor) {
        if (valor instanceof LocalDate) {
            return EXIBE_DATA.format((LocalDate) valor);
        }
        if (valor instanceof LocalTime) {
            return EXIBE_HORA.format((LocalTime) valor);
        }
        return valor == null ? "" : valor.toString();
    }

    private static String csv(String valor) {
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }

    // ------------------------------------------------------------------ utilidades de tela

    private void definirOcupado(boolean ocupado) {
        progresso.setVisible(ocupado);
        botaoLer.setEnabled(!ocupado);
        botaoSelecionar.setEnabled(!ocupado);
        if (ocupado) {
            botaoExportar.setEnabled(false);
            status.setText("Lendo arquivo...");
        }
        setCursor(Cursor.getPredefinedCursor(ocupado ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private static boolean ehDigito(int c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Aceita apenas os caracteres permitidos e limita o tamanho. Ao colar um texto com máscara
     * (como "123.456.789-19"), os caracteres não permitidos são descartados em vez de rejeitar tudo.
     */
    private static final class FiltroTexto extends DocumentFilter {

        private final IntPredicate permitido;
        private final int maximo;

        FiltroTexto(IntPredicate permitido, int maximo) {
            this.permitido = permitido;
            this.maximo = maximo;
        }

        @Override
        public void insertString(FilterBypass fb, int posicao, String texto, AttributeSet atributos)
                throws BadLocationException {
            replace(fb, posicao, 0, texto, atributos);
        }

        @Override
        public void replace(FilterBypass fb, int posicao, int tamanho, String texto, AttributeSet atributos)
                throws BadLocationException {
            String novo = texto == null ? "" : texto;
            StringBuilder aceito = new StringBuilder();
            novo.codePoints().filter(permitido).forEach(aceito::appendCodePoint);
            int livre = Math.max(0, maximo - (fb.getDocument().getLength() - tamanho));
            if (aceito.length() > livre) {
                aceito.setLength(livre);
            }
            if (aceito.length() == 0 && !novo.isEmpty()) {
                return; // nada do que foi digitado é permitido
            }
            super.replace(fb, posicao, tamanho, aceito.toString(), atributos);
        }
    }
}
