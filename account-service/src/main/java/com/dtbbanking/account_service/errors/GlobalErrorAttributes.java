package com.dtbbanking.account_service.errors;

import com.dtbbanking.account_service.dto.UniversalResponse;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);

        int status;
        String message;
        Object data = null;

        if (error instanceof DuplicateResourceException dupEx) {
            status = HttpStatus.CONFLICT.value();
            message = dupEx.getMessage();

        } else if (error instanceof WebExchangeBindException validationEx) {
            status = HttpStatus.BAD_REQUEST.value();
            message = "Validation failed";
            data = validationEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fe -> Map.of(
                            "field", fe.getField(),
                            "message", Objects.requireNonNull(fe.getDefaultMessage()),
                            "rejectedValue", fe.getRejectedValue()
                    )).toList();

        } else if (error instanceof CustomerNotFoundException notFoundEx) {
            status = HttpStatus.NOT_FOUND.value();
            message = notFoundEx.getMessage();

        } else if (error instanceof ResponseStatusException rsEx && rsEx.getStatusCode() == HttpStatus.NOT_FOUND) {
            // Generic 404 from missing route
            status = HttpStatus.NOT_FOUND.value();
            message = "Resource not found";

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            message = "An unexpected error occurred";
        }

        UniversalResponse<Object> response = UniversalResponse.error(
                status,
                message,
                data
        );

        // Convert UniversalResponse to Map
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("status", response.status());
        errorAttributes.put("message", response.message());
        errorAttributes.put("data", response.data());
        errorAttributes.put("timestamp", LocalDateTime.now());
        errorAttributes.put("path", request.path());

        return errorAttributes;
    }
}


