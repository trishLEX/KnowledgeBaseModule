package ru.fa.scripts;

import ru.fa.scripts.model.DimensionType;

public class BankingProductImport {

    public static void main(String[] args) throws Exception {
        CommonImport.importData("BankingProduct", DimensionType.BANKING_PRODUCT, 1, "_OpenDateAccount");
    }
}
