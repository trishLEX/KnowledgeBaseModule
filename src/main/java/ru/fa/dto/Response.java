package ru.fa.dto;

public class Response {

    private Status status;
    private Object result;

    public Response(Status status, Object result) {
        this.status = status;
        this.result = result;
    }

    public Status getStatus() {
        return status;
    }

    public Object getResult() {
        return result;
    }

    public enum Status {
        OK,
        ERROR
    }
}
