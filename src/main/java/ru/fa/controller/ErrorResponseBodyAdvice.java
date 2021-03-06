package ru.fa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.fa.dto.Response;
import ru.fa.exception.BadRequestException;
import ru.fa.exception.NotFoundException;

@ControllerAdvice
public class ErrorResponseBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(ErrorResponseBodyAdvice.class);

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response badRequest(BadRequestException ex) {
        return new Response(Response.Status.ERROR, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Response notFound(NotFoundException ex) {
        return new Response(Response.Status.ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Response exception(Exception ex) {
        log.error("Exception occurred", ex);
        return new Response(Response.Status.ERROR, ex.getMessage());
    }
}
