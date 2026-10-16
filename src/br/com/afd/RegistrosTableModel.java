package br.com.afd;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Modelo da tabela de resultados. */
final class RegistrosTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    static final int COL_LINHA = 0;
    static final int COL_NSR = 1;
    static final int COL_TIPO = 2;
    static final int COL_EVENTO = 3;
    static final int COL_DATA = 4;
    static final int COL_HORA = 5;
    static final int COL_DOCUMENTO = 6;
    static final int COL_NOME = 7;

    private static final String[] NOMES = {"Linha", "NSR", "Tipo", "Evento", "Data", "Hora", null, "Nome"};
    private static final Class<?>[] CLASSES = {
        Integer.class, String.class, String.class, String.class,
        LocalDate.class, LocalTime.class, String.class, String.class
    };

    private transient List<RegistroAfd> registros = Collections.emptyList();
    private Portaria portaria = Portaria.P1510;

    void setDados(List<RegistroAfd> novos, Portaria novaPortaria) {
        this.registros = novos;
        this.portaria = novaPortaria;
        fireTableStructureChanged();
    }

    List<RegistroAfd> getRegistros() {
        return registros;
    }

    Portaria getPortaria() {
        return portaria;
    }

    @Override
    public int getRowCount() {
        return registros.size();
    }

    @Override
    public int getColumnCount() {
        return NOMES.length;
    }

    @Override
    public String getColumnName(int coluna) {
        return coluna == COL_DOCUMENTO ? portaria.getRotuloDocumento() : NOMES[coluna];
    }

    @Override
    public Class<?> getColumnClass(int coluna) {
        return CLASSES[coluna];
    }

    @Override
    public Object getValueAt(int linha, int coluna) {
        RegistroAfd r = registros.get(linha);
        switch (coluna) {
            case COL_LINHA:
                return r.getNumeroLinha();
            case COL_NSR:
                return r.getNsr();
            case COL_TIPO:
                return String.valueOf(r.getTipoRegistro());
            case COL_EVENTO:
                return r.getEvento().getDescricao();
            case COL_DATA:
                return r.getDataHora().toLocalDate();
            case COL_HORA:
                return r.getDataHora().toLocalTime();
            case COL_DOCUMENTO:
                return Documentos.formatar(r.getDocumento(), portaria);
            case COL_NOME:
                return r.getNome();
            default:
                return null;
        }
    }
}
