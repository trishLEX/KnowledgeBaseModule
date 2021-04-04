package ru.fa.scripts;

import ru.fa.scripts.model.DimensionType;

public class OperationChannelImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("OperationChannel", DimensionType.OPERATION_CHANNEL, 432, null);
    }
}
