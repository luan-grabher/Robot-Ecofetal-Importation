from fileManager.fileManager import textHasStringFilter, findFile
from configparser import ConfigParser
import builtins
import os
import pandas as pd

# read ini EcofetalReceitasTaxaPadrao.ini
config = ConfigParser()

log = ""


def print(message):
    global log
    log += message + "\n"
    # call built-in print function
    builtins.print(message)
    return log


# Def function to get data from excel
def getExcelData(file_path, usecols, columns_to_read, header=1, prepareRowFunction=None):
    data = []

    # using pandas to read xlsx file sheet 1 using second row as header
    df = pd.read_excel(
        file_path, sheet_name=0, usecols=usecols, header=header)

    # For each row in the dataframe
    for index, row in df.iterrows():
        # Se a linha tiver vazias, tenta copiar a linha anterior
        if row.isnull().sum() > 0:
            #row is prepareRowFunction passing row, index and df if prepareRowFunction is defined and is Function
            if prepareRowFunction and callable(prepareRowFunction):
                row = prepareRowFunction(row, index, df)

        item = {}
        
        # Se a row não estiver vazia
        if row.isnull().sum() == 0:
            for column in columns_to_read:
                # if the column[0] is not 'use columns'
                if column[0] != 'use columns':
                    columns_data = ''
                    # split the column[1] by '|' and trim each element
                    column_to_read = [x.strip()
                                        for x in column[1].split('|')]
                    # for each element in column_to_read, join in columns_data with ' '
                    for element in column_to_read:
                        columns_data += str(row[element]) + ' '

                    # trim the columns_data
                    columns_data = columns_data.strip()                    
                    item[column[0]] = columns_data

            # append item to data
            data.append(item)
    return data

# Função para corrigir row com valores incorretos
def prepareRowReceipt(row, index, df):
    tipo_pagamento = str(row['Tipo pagamento'])
    #Se o 'Tipo pagamento' não for None ou '' e os outros campos estão vazios, copia a linha anterior para a linha atual e mantém o campo 'Tipo pagamento'
    if tipo_pagamento != 'nan' and str(row['Nome']) == 'nan' and str(row['Emissão']) == 'nan':
        #while row - 1 has 'Nome' and 'Emissão' nan, decrease index
        while str(df.iloc[index-1]['Nome']) == 'nan' and str(df.iloc[index-1]['Emissão']) == 'nan':
            index -= 1
        newRow = df.iloc[index-1].copy()
        newRow['Tipo pagamento'] = tipo_pagamento
        return newRow
    else:
        return row

'''
    #get data from receipts or taxs file
'''
def excelDataToCsv(folder_path, file_name, sectionsName, prepareRowFunction=None):
    file_path = os.path.join(folder_path, file_name)
    columns = config.items(sectionsName + ' columns')
    usecols = config.get(sectionsName + ' columns', 'use columns')
    accounts = config.items(sectionsName + ' accounts')

    # get data from receipts file
    data = getExcelData(
        file_path, usecols, columns, prepareRowFunction=prepareRowFunction)
    
    #Normalize the data
    for row in data:
        #convert 'date' to datetime
        row['date'] = pd.to_datetime(row['date'])
        #convert datetime to string in format dd/mm/yyyy
        row['date'] = row['date'].strftime('%d/%m/%Y')

        #If has not 'debit' or 'credit', set 'debit' and 'credit' to 0
        if not 'debit' in row:
            row['debit'] = 'debit'
        if not 'credit' in row:
            row['credit'] = 'credit'    

        #for each account in accounts, if field 'credit' or 'debit' has string filter in value of 'account', set value of field to key of account
        for account in accounts:
            if textHasStringFilter(str(row['credit']), account[1]):
                row['credit'] = int(account[0])
            if textHasStringFilter(str(row['debit']), account[1]):
                row['debit'] = int(account[0])
        
        #if field 'credit' or 'debit' is string, set value to 0
        if isinstance(row['credit'], str):
            row['credit'] = 0
        if isinstance(row['debit'], str):
            row['debit'] = 0
    
    #save data to csv file in folder_path using ';' as separator and utf-8 encoding with file_name as 'sectionsName.csv'
    df = pd.DataFrame(data)
    df.to_csv(os.path.join(folder_path, sectionsName + '.csv'), sep=';', encoding='utf-8', index=False)

    #print save message
    print('Arquivo ' + file_name + ' salvo em ' + folder_path)
    

# Receitas e Taxas Padrao
def EcofetalReceitasTaxaPadrao(month, year, inipath='ecofetal-receitas-taxa-padrao.ini'):
    '''try:'''
    config.read(inipath, encoding='utf-8')

    if config.sections():
        # in config file set paths.month to month with zero padding
        config.set('paths', 'month', str(int(month)).zfill(2))
        # in config file set paths.year to year
        config.set('paths', 'year', str(int(year)))

        # Get paths.receipts and paths.tax
        receipts_folder = config.get('paths', 'receipts')
        tax_folder = config.get('paths', 'tax')

        # check if folders paths.receipts and paths.tax exist
        if os.path.exists(receipts_folder) and os.path.exists(tax_folder):
            receipts_file_filter = config.get('files', 'receipts')
            tax_file_filter = config.get('files', 'tax')

            # find receipts file
            receipts_file = findFile(receipts_folder, receipts_file_filter)
            # find tax file
            tax_file = findFile(tax_folder, tax_file_filter)

            # check if receipts file exists
            if receipts_file:
                # convert excel file to csv file
                excelDataToCsv(receipts_folder, receipts_file, 'receipts', prepareRowFunction=prepareRowReceipt)
            else:
                print(
                    "Arquivo de receitas não encontrado com o filtro: " + receipts_file_filter)

            # check if tax file exists
            if tax_file:
                # convert excel file to csv file
                excelDataToCsv(tax_folder, tax_file, 'tax')
            else:
                print(
                    'Arquivo de taxas não encontrado com o filtro: ' + tax_file_filter)

            return log
        else:
            return print("Pasta de receitas não existe:\n '" + receipts + "'\n ou pasta de taxas não existe:\n '" + tax + "'")

    else:
        return print("Arquivo de configuração  '" + inipath + "' não encontrado.")
    '''except Exception as e:
        return print("Erro inesperado:\n" + str(e))'''


EcofetalReceitasTaxaPadrao(6, 2022)
