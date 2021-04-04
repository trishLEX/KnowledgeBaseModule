package ru.fa.scripts;

import ru.fa.scripts.model.DimensionType;

public class OperationKindImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("OperationKind", DimensionType.OPERATION_KIND, 117, null);
    }
}
