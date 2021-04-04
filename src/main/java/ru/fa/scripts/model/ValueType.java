package ru.fa.scripts.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ValueType {
    @JsonProperty("LIMIT_VALUE")
    LIMIT_VALUE,

    @JsonProperty("FEE_VALUE")
    FEE_VALUE,

    @JsonProperty("TARIFF_VALUE")
    TARIFF_VALUE,

    @JsonProperty("CHANNEL_VALUE")
    CHANNEL_VALUE,

    @JsonProperty("DOCUMENTS_VALUE")
    DOCUMENTS_VALUE,

    @JsonProperty("INFORMATION_VALUE")
    INFORMATION_VALUE,

    @JsonProperty("PERIOD_VALUE")
    PERIOD_VALUE
}
