package ru.fa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.beans.ConstructorProperties;
import java.util.Objects;

public class Value {

    @JsonProperty("id")
    private long id;

    @JsonProperty("strId")
    private String strId;

    @JsonProperty("content")
    private JsonNode content;

    @JsonProperty("type")
    private String type;

    @ConstructorProperties({"id", "strId", "content", "type"})
    public Value(long id, String strId, JsonNode content, String type) {
        this.id = id;
        this.strId = strId;
        this.content = content;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public JsonNode getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return id == value.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Value{" +
                "id=" + id +
                ", strId='" + strId + '\'' +
                ", content=" + content +
                '}';
    }
}
