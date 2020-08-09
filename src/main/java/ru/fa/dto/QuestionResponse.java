package ru.fa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class QuestionResponse {

    @JsonProperty("response_type")
    private QuestionResponseType responseType;

    public QuestionResponse(QuestionResponseType responseType) {
        this.responseType = responseType;
    }

    public QuestionResponseType getResponseType() {
        return responseType;
    }

    public static class Question extends QuestionResponse {

        @JsonProperty("question")
        private String question;

        @JsonProperty("dimension_subtype")
        private String dimensionSubType;

        public Question(String question, String dimensionSubType) {
            super(QuestionResponseType.QUESTION);
            this.question = question;
            this.dimensionSubType = dimensionSubType;
        }

        public String getQuestion() {
            return question;
        }

        public String getDimensionSubType() {
            return dimensionSubType;
        }
    }

    public static class Answer extends QuestionResponse {

        @JsonProperty("str_id")
        private String strId;

        @JsonProperty("content")
        private String content;

        @JsonProperty("value_subtype")
        private String valueSubType;

        public Answer(String strId, String content, String valueSubType) {
            super(QuestionResponseType.QUESTION);
            this.strId = strId;
            this.content = content;
            this.valueSubType = valueSubType;
        }

        public String getStrId() {
            return strId;
        }

        public String getContent() {
            return content;
        }

        public String getValueSubType() {
            return valueSubType;
        }
    }
}
