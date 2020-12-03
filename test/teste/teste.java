package teste;

import importacaoExcelPadrao.EcofetalImportation;
import java.util.Map;
import java.util.HashMap;

public class teste {

    public static void main(String[] args) {
        int mes = 11;
        int ano = 2020;
        String pastaEmpresa = "Ecofetal  Serviço de Auxílio Diagnóstico e Terapia Ltda";
        String pastaAnual = "Movimentos";
        String pastaMensal = "Bancos";
        
        String banco = "Bradesco (9)";
        String idTemplate = "EcofetalBanco9";
        String filtroArquivo = "Extrato;Bradesco;.xlsx";
        
        Map<String, String> colunas = new HashMap<>();
        colunas.put("data", "B");
        //colunas.put("documento", "");
        //colunas.put("pretexto", "B");
        colunas.put("historico", "I;C");
        //colunas.put("entrada", "G");
        //colunas.put("saida", "-H");
        colunas.put("valor", "F");


        System.out.println(EcofetalImportation.principal(
                        mes,
                        ano,
                        pastaEmpresa,
                        pastaAnual,
                        pastaMensal,
                        banco,
                        idTemplate,
                        filtroArquivo,
                        colunas
                ).replaceAll("<br>", "\n")
        );
    }

}
