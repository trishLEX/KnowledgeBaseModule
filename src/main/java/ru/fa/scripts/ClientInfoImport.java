package ru.fa.scripts;

public class ClientInfoImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("ClientInfo", DimensionType.CLIENT_INFO, 448, null);
    }
}
