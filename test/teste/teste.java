package teste;

import EcofetalImportation.EcofetalImportation;

public class teste {

    public static void main(String[] args) {
        EcofetalImportation.mes = 10;
        EcofetalImportation.ano = 2020;

        System.out.println(
                EcofetalImportation.impostos(
                        "IRRF",
                        "V",
                        650,
                        101
                ).replaceAll("<br>", "\n")
        );
    }

}
