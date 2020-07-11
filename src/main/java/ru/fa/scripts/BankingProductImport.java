package ru.fa.scripts;

public class BankingProductImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("BankingProduct", DimensionType.BANKING_PRODUCT, 1, "_OpenDateAccount");
    }
}
