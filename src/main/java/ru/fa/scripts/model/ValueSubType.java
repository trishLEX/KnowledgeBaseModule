package ru.fa.scripts.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ValueSubType {
    @JsonProperty("TRANSACTION_FEE_WITHIN_LIMIT")
    TRANSACTION_FEE_WITHIN_LIMIT,

    @JsonProperty("TRANSACTION_FEE_ABOVE_LIMIT")
    TRANSACTION_FEE_ABOVE_LIMIT,

    @JsonProperty("TRANSACTION_LIMIT_PER_MONTH")
    TRANSACTION_LIMIT_PER_MONTH,

    @JsonProperty("TRANSACTION_LIMIT_PER_DAY")
    TRANSACTION_LIMIT_PER_DAY,

    @JsonProperty("TRANSACTION_LIMIT_PER_OPERATION")
    TRANSACTION_LIMIT_PER_OPERATION,

    @JsonProperty("TARIFF")
    TARIFF,

    @JsonProperty("REQUIRED_DOCUMENTS")
    REQUIRED_DOCUMENTS,

    @JsonProperty("CHANNELS")
    CHANNELS,

    @JsonProperty("INFORMATION")
    INFORMATION,

    @JsonProperty("CREDIT_LIMIT")
    CREDIT_LIMIT,

    @JsonProperty("PERIOD")
    PERIOD
}
