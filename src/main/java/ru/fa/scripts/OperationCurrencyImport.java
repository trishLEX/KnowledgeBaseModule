package ru.fa.scripts;

import ru.fa.scripts.model.DimensionType;

public class OperationCurrencyImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("OperationCurrency", DimensionType.OPERATION_CURRENCY, 443, null);
    }
}
