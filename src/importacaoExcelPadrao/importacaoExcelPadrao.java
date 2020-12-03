package importacaoExcelPadrao;

import Entity.Executavel;
import Robo.AppRobo;
import TemplateContabil.Control.ControleTemplates;
import TemplateContabil.Model.Entity.Importation;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class importacaoExcelPadrao {

    private static String nomeApp = "";
    private static String pastaEmpresa = "Ecofetal  Serviço de Auxílio Diagnóstico e Terapia Ltda";
    private static String pastaAnual = "Movimentos";
    private static String pastaMensal = "Bancos";
    private static int mes;
    private static int ano;

    public static void main(String[] args) {
        try {
            AppRobo robo = new AppRobo(nomeApp);

            robo.definirParametros();

            Map<String, String> colunasCaixaFixo = new HashMap<>();
            colunasCaixaFixo.put("data", "A");
            colunasCaixaFixo.put("pretexto", "B");
            colunasCaixaFixo.put("historico", "F");
            colunasCaixaFixo.put("entrada", "G");
            colunasCaixaFixo.put("saida", "-H");

            mes = robo.getParametro("mes").getMes();
            ano = robo.getParametro("ano").getInteger();
            nomeApp = "Importação Bancos " + pastaEmpresa;

            robo.setNome(nomeApp);
            robo.executar(
                    templateBanco(5, "Caixa Fixo", "CAIXA;FIXO;.xlsx", colunasCaixaFixo) + 
                    templateBanco(5, "Caixa Fixo", "CAIXA;FIXO;.xlsx", colunasCaixaFixo)
            );
        } catch (Exception e) {
            System.out.println("Ocorreu um erro na aplicação: " + e);
            System.exit(0);
        }
    }

    public static String templateBanco(int contaBanco, String nome, String filtroArquivo, Map<String, String> colunas) {
        return principal(
                mes,
                ano,
                pastaEmpresa,
                pastaAnual,
                pastaMensal,
                "Template " + nome +" (" + contaBanco + ")",
                "EcofetalBanco" + contaBanco,
                filtroArquivo,
                colunas
        ) + "\n\n";
    }

    public static String principal(int mes, int ano, String pastaEmpresa, String pastaAnual, String pastaMensal, String banco, String idTemplate, String filtroArquivo, Map<String, String> colunas) {
        try {
            Importation importation = new Importation(Importation.TIPO_EXCEL);
            importation.setIdTemplateConfig(idTemplate);
            importation.getExcelCols().putAll(colunas);
            importation.setNome(banco);

            ControleTemplates controle = new ControleTemplates(mes, ano);
            controle.setPastaEscMensal(pastaEmpresa);
            controle.setPasta(pastaAnual, pastaMensal);

            Map<String, Executavel> execs = new LinkedHashMap<>();
            execs.put("Procurando arquivo", controle.new defineArquivoNaImportacao(filtroArquivo, importation));
            execs.put("Criando template", controle.new converterArquivoParaTemplate(importation));

            return AppRobo.rodarExecutaveis(nomeApp, execs);
        } catch (Exception e) {
            return "Ocorreu um erro no Java: " + e;
        }
    }

}
