package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.relatorio;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ExportacaoRelatorio;

/**
 * Gera um PDF simples, em A4 paisagem, com as fontes padrão do leitor (Helvetica). Os textos usam a codificação
 * WinAnsi, que cobre os acentos do português.
 */
final class ExportadorPdf {
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final float LARGURA = 842;
    private static final float ALTURA = 595;
    private static final float MARGEM = 40;
    private static final float CORPO = 8.5f;
    private static final float LINHA = 13;

    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder atual = new StringBuilder();
    private float y;

    private ExportadorPdf() {
        novaPagina();
    }

    static byte[] gerar(ExportacaoRelatorio exportacao) {
        var pdf = new ExportadorPdf();
        pdf.texto(exportacao.titulo(), 16, true, 0);
        pdf.y -= 6;
        for (var linha : exportacao.cabecalho()) {
            pdf.texto(linha, 9, false, 0);
        }
        for (var tabela : exportacao.tabelas()) {
            pdf.tabela(tabela);
        }
        pdf.texto("Dados pessoais protegidos pela LGPD: CPF e telefone mascarados; informações de saúde não são exportadas.",
                7.5f, false, 0);
        return pdf.montar();
    }

    private void novaPagina() {
        atual = new StringBuilder();
        paginas.add(atual);
        y = ALTURA - MARGEM;
    }

    private void garantirEspaco(float necessario) {
        if (y - necessario < MARGEM) {
            novaPagina();
        }
    }

    private void texto(String conteudo, float tamanho, boolean negrito, float x) {
        garantirEspaco(tamanho + 6);
        escrever(conteudo, tamanho, negrito, MARGEM + x, y - tamanho);
        y -= tamanho + 6;
    }

    private void escrever(String conteudo, float tamanho, boolean negrito, float x, float base) {
        atual.append("BT /").append(negrito ? "F2" : "F1").append(' ').append(numero(tamanho)).append(" Tf ")
                .append(numero(x)).append(' ').append(numero(base)).append(" Td (").append(escapar(conteudo))
                .append(") Tj ET\n");
    }

    private void linhaHorizontal(float posicao) {
        atual.append("0.75 G 0.5 w ").append(numero(MARGEM)).append(' ').append(numero(posicao)).append(" m ")
                .append(numero(LARGURA - MARGEM)).append(' ').append(numero(posicao)).append(" l S 0 G\n");
    }

    private void tabela(ExportacaoRelatorio.Tabela tabela) {
        y -= 8;
        garantirEspaco(LINHA * 3 + 12);
        texto(tabela.titulo(), 11, true, 0);
        var larguraColuna = (LARGURA - 2 * MARGEM) / Math.max(1, tabela.colunas().size());
        cabecalho(tabela, larguraColuna);
        if (tabela.linhas().isEmpty()) {
            texto("Nenhum registro.", CORPO, false, 0);
            return;
        }
        for (var linha : tabela.linhas()) {
            if (y - LINHA < MARGEM) {
                novaPagina();
                cabecalho(tabela, larguraColuna);
            }
            for (int coluna = 0; coluna < linha.size(); coluna++) {
                escrever(cortar(linha.get(coluna), larguraColuna, CORPO), CORPO, coluna == 0, MARGEM + coluna * larguraColuna,
                        y - CORPO);
            }
            y -= LINHA;
            linhaHorizontal(y + 3);
        }
    }

    private void cabecalho(ExportacaoRelatorio.Tabela tabela, float larguraColuna) {
        for (int coluna = 0; coluna < tabela.colunas().size(); coluna++) {
            escrever(cortar(tabela.colunas().get(coluna), larguraColuna, CORPO), CORPO, true,
                    MARGEM + coluna * larguraColuna, y - CORPO);
        }
        y -= LINHA;
        linhaHorizontal(y + 3);
    }

    /** Corta o texto para caber na coluna, considerando a largura média dos caracteres da Helvetica. */
    private static String cortar(String texto, float largura, float tamanho) {
        int maximo = (int) ((largura - 6) / (tamanho * 0.52f));
        return texto.length() <= maximo ? texto : texto.substring(0, Math.max(1, maximo - 1)) + "…";
    }

    private static String escapar(String texto) {
        var normalizado = texto.replace('→', '>').replace('−', '-').replace(' ', ' ');
        var bytes = normalizado.getBytes(WIN_ANSI);
        var resultado = new StringBuilder();
        for (byte valor : bytes) {
            int codigo = valor & 0xff;
            if (codigo == '(' || codigo == ')' || codigo == '\\') {
                resultado.append('\\').append((char) codigo);
            } else if (codigo < 32 || codigo > 126) {
                resultado.append('\\').append(String.format(Locale.ROOT, "%03o", codigo));
            } else {
                resultado.append((char) codigo);
            }
        }
        return resultado.toString();
    }

    private static String numero(float valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }

    private byte[] montar() {
        var objetos = new ArrayList<String>();
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");
        var filhos = new StringBuilder();
        int primeiraPagina = 5;
        for (int indice = 0; indice < paginas.size(); indice++) {
            filhos.append(primeiraPagina + indice * 2).append(" 0 R ");
        }
        objetos.add("<< /Type /Pages /Kids [" + filhos + "] /Count " + paginas.size() + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        for (int indice = 0; indice < paginas.size(); indice++) {
            int conteudo = primeiraPagina + indice * 2 + 1;
            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + (int) LARGURA + " " + (int) ALTURA
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + conteudo + " 0 R >>");
            var fluxo = paginas.get(indice).toString();
            objetos.add("<< /Length " + fluxo.getBytes(StandardCharsets.ISO_8859_1).length + " >>\nstream\n" + fluxo
                    + "endstream");
        }
        var saida = new ByteArrayOutputStream();
        var posicoes = new ArrayList<Integer>();
        escreverBytes(saida, "%PDF-1.4\n%âãÏÓ\n");
        for (int indice = 0; indice < objetos.size(); indice++) {
            posicoes.add(saida.size());
            escreverBytes(saida, (indice + 1) + " 0 obj\n" + objetos.get(indice) + "\nendobj\n");
        }
        int xref = saida.size();
        var tabela = new StringBuilder("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (var posicao : posicoes) {
            tabela.append(String.format(Locale.ROOT, "%010d 00000 n \n", posicao));
        }
        tabela.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref)
                .append("\n%%EOF\n");
        escreverBytes(saida, tabela.toString());
        return saida.toByteArray();
    }

    private static void escreverBytes(ByteArrayOutputStream saida, String texto) {
        saida.writeBytes(texto.getBytes(StandardCharsets.ISO_8859_1));
    }
}
