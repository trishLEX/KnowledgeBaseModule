package ru.fa.scripts;

public enum DimensionSubType {
    
    CARD_CATEGORY(DimensionType.BANKING_PRODUCT),
    CARD_TYPE(DimensionType.BANKING_PRODUCT),
    CARD_PRODUCT(DimensionType.BANKING_PRODUCT),
    CARD_PAYMENT_SYSTEM(DimensionType.BANKING_PRODUCT),
    CARD_SPECIFIC(DimensionType.BANKING_PRODUCT),
    CARD_CURRENCY(DimensionType.BANKING_PRODUCT),
    CARD_LEVEL(DimensionType.BANKING_PRODUCT),
    CARD_BANK(DimensionType.BANKING_PRODUCT),
    CARD_SPECIAL_TARIFF_PLAN(DimensionType.BANKING_PRODUCT),
    CARD_SERVICE_PACKAGE(DimensionType.BANKING_PRODUCT),
    CARD_INDIVIDUAL_DESIGN(DimensionType.BANKING_PRODUCT),
    CARD_TRANSPORT_APP(DimensionType.BANKING_PRODUCT),
    TYPE_OF_OFFER(DimensionType.BANKING_PRODUCT),

    CARD_OPERATION(DimensionType.OPERATION_KIND),

    OPERATION_PLACE_GEO(DimensionType.OPERATION_PLACE_GEO),

    OPERATION_PLACE_BANK(DimensionType.OPERATION_PLACE_BANK),

    OPERATION_CHANNEL(DimensionType.OPERATION_CHANNEL),

    OPERATION_CURRENCY(DimensionType.OPERATION_CURRENCY),

    CITIZENSHIP(DimensionType.CLIENT_INFO),
    AGE(DimensionType.CLIENT_INFO),
    CLIENT_CATEGORY(DimensionType.CLIENT_INFO),
    CLIENT_TYPE(DimensionType.CLIENT_INFO),
    REGISTRATION(DimensionType.CLIENT_INFO)
    ;
    
    DimensionSubType(DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    };

    private final DimensionType dimensionType;

    public DimensionType getDimensionType() {
        return dimensionType;
    }
}
