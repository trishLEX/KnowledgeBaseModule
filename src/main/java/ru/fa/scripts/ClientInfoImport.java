package ru.fa.scripts;

import ru.fa.scripts.model.DimensionType;

public class ClientInfoImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("ClientInfo", DimensionType.CLIENT_INFO, 448, null);
    }
}
