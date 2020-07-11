package ru.fa.scripts;

public class OperationChannelImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("OperationChannel", DimensionType.OPERATION_CHANNEL, 432, null);
    }
}
