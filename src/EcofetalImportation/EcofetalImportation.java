package EcofetalImportation;

import Entity.Executavel;
import Robo.AppRobo;
import TemplateContabil.Control.ControleTemplates;
import TemplateContabil.Model.Entity.Importation;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class EcofetalImportation {

    private static String nomeApp = "";
    private static final String pastaEmpresa = "Ecofetal  Serviço de Auxílio Diagnóstico e Terapia Ltda";
    private static final String pastaAnual = "Movimentos";
    private static final String pastaMensal = "Bancos";
    private static int mes;
    private static int ano;

    public static void main(String[] args) {
        try {
            AppRobo robo = new AppRobo(nomeApp);

            robo.definirParametros();

            /**
             * ----------------------- COLUNAS --------------------*
             */
            Map<String, String> colunasCaixaFixo = new HashMap<>();
            colunasCaixaFixo.put("data", "A");
            colunasCaixaFixo.put("pretexto", "B");
            colunasCaixaFixo.put("historico", "F");
            colunasCaixaFixo.put("entrada", "G");
            colunasCaixaFixo.put("saida", "-H");

            Map<String, String> colunasBBMovimento = new HashMap<>();
            colunasBBMovimento.put("data", "B");
            colunasBBMovimento.put("historico", "I");
            colunasBBMovimento.put("valor", "G");           
        
            Map<String, String> colunasBBConvenio = new HashMap<>();
            colunasBBConvenio.put("data", "B");
            colunasBBConvenio.put("historico", "I;C");
            colunasBBConvenio.put("valor", "E");            

            Map<String, String> colunasBradesco = new HashMap<>();
            colunasBradesco.put("data", "B");
            colunasBradesco.put("historico", "I;C");
            colunasBradesco.put("valor", "F");
            
            Map<String, String> colunasCEF = new HashMap<>();
            colunasCEF.put("data", "B");
            colunasCEF.put("historico", "I;C");
            colunasCEF.put("valor", "F");
            
            Map<String, String> colunasServicoop = new HashMap<>();
            colunasServicoop.put("data", "B");
            colunasServicoop.put("historico", "J;C");
            colunasServicoop.put("valor", "F");
            
            Map<String, String> colunasSantander = new HashMap<>();
            colunasSantander.put("data", "B");
            colunasSantander.put("historico", "C");
            colunasSantander.put("valor", "F");

            /**
             * ----------------------- TERMINA COLUNAS --------------------*
             */
            mes = robo.getParametro("mes").getMes();
            ano = robo.getParametro("ano").getInteger();
            nomeApp = "Importação Bancos " + pastaEmpresa;

            robo.setNome(nomeApp);
            robo.executar(
                    templateBanco(5, "Caixa Fixo", "CAIXA;FIXO;.xlsx", colunasCaixaFixo)
                    + templateBanco(7, "BB Movimento", "Extrato;BB;Movimento;.xlsx", colunasBBMovimento)
                    + templateBanco(9, "Bradesco", "Extrato;Bradesco;.xlsx", colunasBradesco)
                    + templateBanco(10, "CEF", "Extrato;Caixa;Federal;.xlsx", colunasCEF)
                    + templateBanco(11, "Santander", "SANTANDER;.xlsx", colunasSantander)
                    + templateBanco(2078, "BB Convenio", "Extrato;BB;Convenio;.xlsx", colunasBBConvenio)
                    + templateBanco(2138, "Servicoop", "SERVICOOP;.xlsx", colunasServicoop)
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
                "Template " + nome + " (" + contaBanco + ")",
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
            execs.put("Procurar arquivo " + banco, controle.new defineArquivoNaImportacao(filtroArquivo, importation));
            execs.put("Criar Template", controle.new converterArquivoParaTemplate(importation));

            return AppRobo.rodarExecutaveis(nomeApp, execs);
        } catch (Exception e) {
            return "Ocorreu um erro no Java: " + e;
        }
    }

}
