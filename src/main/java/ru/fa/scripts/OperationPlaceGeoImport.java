package ru.fa.scripts;

import ru.fa.model.DimensionType;

public class OperationPlaceGeoImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("OperationPlaceGeo", DimensionType.OPERATION_PLACE_GEO, 234, null);
    }
}
