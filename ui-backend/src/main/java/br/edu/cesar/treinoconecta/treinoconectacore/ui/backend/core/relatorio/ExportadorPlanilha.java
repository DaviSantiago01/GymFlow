package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.relatorio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ExportacaoRelatorio;

/** Gera uma planilha .xlsx (Office Open XML) com o cabeçalho e as tabelas do relatório em uma única aba. */
final class ExportadorPlanilha {
    private static final int TITULO = 1;
    private static final int NEGRITO = 2;

    private final StringBuilder linhas = new StringBuilder();
    private int linhaAtual;
    private int colunasMaximas = 1;

    private ExportadorPlanilha() {
    }

    static byte[] gerar(ExportacaoRelatorio exportacao) {
        var planilha = new ExportadorPlanilha();
        planilha.linha(List.of(exportacao.titulo()), TITULO);
        for (var linha : exportacao.cabecalho()) {
            planilha.linha(List.of(linha), 0);
        }
        for (var tabela : exportacao.tabelas()) {
            planilha.linha(List.of(), 0);
            planilha.linha(List.of(tabela.titulo()), NEGRITO);
            planilha.linha(tabela.colunas(), NEGRITO);
            for (var linha : tabela.linhas()) {
                planilha.linha(linha, 0);
            }
        }
        planilha.linha(List.of(), 0);
        planilha.linha(List.of("Dados pessoais protegidos pela LGPD: CPF e telefone mascarados; informações de saúde não são exportadas."), 0);
        return planilha.montar();
    }

    private void linha(List<String> celulas, int estilo) {
        linhaAtual++;
        colunasMaximas = Math.max(colunasMaximas, celulas.size());
        linhas.append("<row r=\"").append(linhaAtual).append("\">");
        for (int coluna = 0; coluna < celulas.size(); coluna++) {
            linhas.append("<c r=\"").append(referencia(coluna)).append(linhaAtual).append("\" t=\"inlineStr\"");
            if (estilo > 0) {
                linhas.append(" s=\"").append(estilo).append('"');
            }
            linhas.append("><is><t xml:space=\"preserve\">").append(xml(celulas.get(coluna))).append("</t></is></c>");
        }
        linhas.append("</row>");
    }

    private static String referencia(int coluna) {
        var texto = new StringBuilder();
        int valor = coluna + 1;
        while (valor > 0) {
            int resto = (valor - 1) % 26;
            texto.insert(0, (char) ('A' + resto));
            valor = (valor - 1) / 26;
        }
        return texto.toString();
    }

    private static String xml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private byte[] montar() {
        var colunas = new StringBuilder("<cols>");
        for (int coluna = 1; coluna <= colunasMaximas; coluna++) {
            colunas.append("<col min=\"").append(coluna).append("\" max=\"").append(coluna).append("\" width=\"")
                    .append(coluna == 1 ? 34 : 20).append("\" customWidth=\"1\"/>");
        }
        colunas.append("</cols>");
        var saida = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(saida)) {
            adicionar(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                    <Default Extension="xml" ContentType="application/xml"/>
                    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                    </Types>""");
            adicionar(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>""");
            adicionar(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets><sheet name="Relatório" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>""");
            adicionar(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                    </Relationships>""");
            adicionar(zip, "xl/styles.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                    <fonts count="3"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="14"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts>
                    <fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
                    <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                    <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                    <cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/><xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/></cellXfs>
                    </styleSheet>""");
            adicionar(zip, "xl/worksheets/sheet1.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">"""
                    + colunas + "<sheetData>" + linhas + "</sheetData></worksheet>");
        } catch (IOException excecao) {
            throw new UncheckedIOException(excecao);
        }
        return saida.toByteArray();
    }

    private static void adicionar(ZipOutputStream zip, String nome, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(nome));
        zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
