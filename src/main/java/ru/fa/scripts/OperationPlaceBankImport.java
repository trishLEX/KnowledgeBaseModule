package ru.fa.scripts;

public class OperationPlaceBankImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("OperationPlaceBank", DimensionType.OPERATION_PLACE_BANK, 422, null);
    }
}
