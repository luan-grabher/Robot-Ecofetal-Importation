package EcofetalImportation.Controller.Model;

import Auxiliar.Valor;
import Entity.Executavel;
import Entity.Warning;
import Robo.View.roboView;
import TemplateContabil.Model.Entity.Importation;
import TemplateContabil.Model.Entity.LctoTemplate;
import TemplateContabil.Model.ImportationModel;
import fileManager.FileManager;
import java.io.File;


public class CriarCsvImposto extends Executavel {

    private final Importation importation;
    private final Integer month;
    private final Integer year;
    private final Integer accountDebit;
    private final Integer history;

    public CriarCsvImposto(Importation importation, Integer month, Integer year, Integer accountDebit, Integer history) {
        this.importation = importation;
        this.month = month;
        this.year = year;
        this.accountDebit = accountDebit;
        this.history = history;
    }
     

    @Override
    public void run() {
        //Chama o modelo da importação que irá criar o template e gerar warning se algo der errado
        ImportationModel modelo = new ImportationModel(importation.getNome(), null, null, importation, null);

        //Percorre lançamentos
        StringBuilder csvText = new StringBuilder();
        for (LctoTemplate lcto : importation.getLctos()) {
            
            //Arruma data
            String dataStr = lcto.getData();
            Valor dataValor = new Valor(dataStr);
            String sqlDate = dataValor.getSQLDate();
            if(!sqlDate.contains(year + "-" + month)){
                sqlDate = year + "-" + month  + "-1";
            }            
            
            csvText.append(657).append(";"); //Codigo Empresa
            csvText.append("").append(";"); //Participante Debito
            csvText.append("").append(";"); //Participante Credito
            csvText.append(sqlDate).append(";"); //Data
            csvText.append(accountDebit).append(";"); //Conta Debito
            csvText.append(747).append(";"); //Conta Credito
            csvText.append(lcto.getDocumento()).append(";"); //Documento
            csvText.append(history).append(";"); //Historico padrao
            csvText.append(lcto.getHistorico()).append(";"); //Complemento
            csvText.append(lcto.getValor().getString().replaceAll("\\.", "\\,")).append(""); //Valor
            csvText.append("\r\n");
        }
        
        
        File file = new File(importation.getFile().getParent() + "\\Importacao " + importation.getNome() + ".csv");
        if(FileManager.save(file, csvText.toString())){
            throw new Warning("Arquivo CSV " + importation.getNome() + " salvo em: " + roboView.link(file.getParentFile()));
        }else{
            throw new Error("Erro ao salvar o arquivo CSV " + importation.getNome() + " em: " + roboView.link(file.getParentFile()));
        }        
    }
}
