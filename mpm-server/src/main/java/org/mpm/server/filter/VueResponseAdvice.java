package org.mpm.server.filter;

import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class VueResponseAdvice implements ResponseBodyAdvice {
    @Value("${needVue:false}")
    Boolean needVue;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return needVue;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (!request.getURI().toString().contains("/api/")) {
            return body; // skip not api
        }
        return NutMap.NEW().setv("code", 0).setv("data", body);
    }
}
