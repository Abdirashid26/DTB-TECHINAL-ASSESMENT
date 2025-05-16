package com.dtbbanking.customer_service.errors;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;
import java.util.Objects;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        if (error instanceof DuplicateResourceException dupEx) {
            errorAttributes.put("status", HttpStatus.CONFLICT.value());
            errorAttributes.put("error", "Conflict");
            errorAttributes.put("message", dupEx.getMessage());
        } else if (error instanceof WebExchangeBindException validationEx) {
            errorAttributes.put("status", HttpStatus.BAD_REQUEST.value());
            errorAttributes.put("error", "Validation Failed");
            errorAttributes.put("message", "Invalid input");
            errorAttributes.put("fieldErrors", validationEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fe -> Map.of(
                            "field", fe.getField(),
                            "message", Objects.requireNonNull(fe.getDefaultMessage()),
                            "rejectedValue", Objects.requireNonNull(fe.getRejectedValue())
                    )).toList());
        }

        return errorAttributes;
    }
}
