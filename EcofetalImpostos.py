
import sys
import glob
import pandas as pd
import traceback
from robotpy.Robot import Robot
from EcofetalImportacao.EcofetalReceitasTaxaPadrao import EcofetalReceitasTaxaPadrao
from configparser import ConfigParser


'''
#PARA TESTES:
class Robot(object):
    def __init__(self):
        self.parameters = {
            'mes': '10',
            'ano': '2022',
        }

    def setReturn(self,  msg):
        print(msg)

robot = Robot()
'''

# Initialize Robot with call_id(first argument)
robot = Robot(sys.argv[1])

try:
    config = ConfigParser()
    config.read("EcofetalImpostos.ini", encoding='utf-8')

    # Get 'mes' from parameters and put zero in front of mes if it is less than 10
    mes = robot.parameters['mes']
    if int(mes) < 10:
        mes = '0' + str(mes)
    mes = str(mes)

    config.set('paths', 'month', mes)

    # Get 'ano' from parameters
    ano = str(robot.parameters['ano'])
    config.set('paths', 'year', ano)

    path = config.get('paths', 'monthPath')

    # get first file with path + config.get('paths', 'fileFilter
    filter = path + '\\' + config.get('paths', 'fileFilter')
    file = glob.glob(filter)

    # if file is not empty
    if file:
        file = file[0]

        # Impostos list
        impostos = {
            'IRRF': {'coluna': 'IRRF', 'conta': '650', 'hist': '101', 'csv': ''},
            'ISSQN': {'coluna': 'ISSQN', 'conta': '1310', 'hist': '102', 'csv': ''},
            'PIS': {'coluna': 'PIS', 'conta': '646', 'hist': '103',    'csv': ''},
            'COFINS': {'coluna': 'COFINS', 'conta': '647', 'hist': '104', 'csv': ''},
            'CSLL': {'coluna': 'CSLL', 'conta': '649', 'hist': '105', 'csv': ''},
        }

        # SQL date format: YYYY-MM-DD
        data = str(ano) + '-' + str(mes) + '-1'

        def createCsvRow(row, imposto):
            text = ';'.join([
                config.get('contas', 'codigo_empresa'),
                '',  # Participante debito
                '',  # Participante credito
                data + '',  # Data
                impostos[imposto]['conta'] + '',  # Conta Debito
                config.get('contas', 'conta_contabil_credito') + \
                '',  # Conta Credito
                row['Nº Duplicata'] + '',  # Documento
                impostos[imposto]['hist'] + '',  # Historico Padrao
                str(row['Nota Fiscal']) + ' ' + \
                row['Descrição'] + '',  # Historico
                str(row[impostos[imposto]['coluna']]
                    ).replace('.', ','),  # Valor
                '\n'
            ])

            return text

        # Read excel file on first sheet with header in row 2
        df = pd.read_excel(file, sheet_name=0, header=1)

        # For each row
        for index, row in df.iterrows():
            # If column 'M', 'E' and 'C' is not empty
            if row['Vencimento'] != '' and row['Descrição'] != '' and row['Nota Fiscal'] != '':
                # if column 'M' contains mes and contains ano
                if str(row['Vencimento']).find(mes) != -1 and str(row['Vencimento']).find(ano) != -1:
                    # For each imposto
                    for imposto in impostos:
                        # If column 'V' is not empty
                        if row[impostos[imposto]['coluna']] != '':
                            # Create csv row
                            csvRow = createCsvRow(row, imposto)
                            # Write csv row
                            impostos[imposto]['csv'] += csvRow

        # For each imposto, create csv utf-8 file and write csv
        for imposto in impostos:
            file = open(
                path + '\\' + impostos[imposto]['coluna'] + '.csv', 'w', encoding='utf-8')
            file.write(impostos[imposto]['csv'])
            file.close()

        retorno = 'Arquivos de Impostos salvos na pasta ' + \
            path + '\n ' + EcofetalReceitasTaxaPadrao(mes, ano)
    else:
        retorno = "Arquivo nao encontrado para o filtro: " + filter
except Exception as e:
    # print stacktrace
    retorno = str(traceback.format_exc())

# Set the json as the return
robot.setReturn(str(retorno))
print(retorno)
sys.exit(0)
