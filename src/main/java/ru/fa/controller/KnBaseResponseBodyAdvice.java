package ru.fa.controller;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import ru.fa.dto.Response;

import javax.annotation.Nonnull;

@ControllerAdvice
public class KnBaseResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object o,
            MethodParameter methodParameter,
            @Nonnull MediaType selectedContentType,
            @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @Nonnull ServerHttpRequest request,
            @Nonnull ServerHttpResponse response
    ) {
        if (methodParameter.getContainingClass().isAnnotationPresent(RestController.class)) {
            if (!(o instanceof Response)) {
                return new Response(Response.Status.OK, o);
            }
        }
        return o;
    }
}
