package ru.fa.scripts;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class Value {

    private int id;
    private String strId;
    private JsonNode content;

    public Value(int id, String strId, JsonNode content) {
        this.id = id;
        this.strId = strId;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public JsonNode getContent() {
        return content;
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
