package ru.fa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.fa.model.DimensionSubType;

public abstract class QuestionResponse {

    @JsonProperty("response_type")
    private QuestionResponseType responseType;

    public QuestionResponse(QuestionResponseType responseType) {
        this.responseType = responseType;
    }

    public static class Question extends QuestionResponse {

        @JsonProperty("question")
        private String question;

        @JsonProperty("dimension_subtype")
        private DimensionSubType dimensionSubType;

        public Question(String question, DimensionSubType dimensionSubType) {
            super(QuestionResponseType.QUESTION);
            this.question = question;
            this.dimensionSubType = dimensionSubType;
        }

        public String getQuestion() {
            return question;
        }

        public DimensionSubType getDimensionSubType() {
            return dimensionSubType;
        }
    }

    public static class Answer extends QuestionResponse {

        //todo answer body

        public Answer() {
            super(QuestionResponseType.ANSWER);
        }
    }
}
