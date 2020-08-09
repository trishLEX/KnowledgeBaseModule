package ru.fa.model;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

public class Value {

    private long id;
    private String strId;
    private JsonNode content;
    private String type;

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
