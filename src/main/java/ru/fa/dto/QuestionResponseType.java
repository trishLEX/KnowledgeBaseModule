package ru.fa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestionResponseType {

    @JsonProperty("QUESTION")
    QUESTION,

    @JsonProperty("ANSWER")
    ANSWER
}
