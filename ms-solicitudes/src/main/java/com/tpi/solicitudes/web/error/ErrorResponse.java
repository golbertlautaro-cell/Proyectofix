package com.tpi.solicitudes.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public Instant timestamp;
    public int status;
    public String error;
    public String message;
    public String path;
    public List<FieldError> fieldErrors;

    public static class FieldError {
        public String field;
        public String message;

        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        ErrorResponse e = new ErrorResponse();
        e.timestamp = Instant.now();
        e.status = status;
        e.error = error;
        e.message = message;
        e.path = path;
        return e;
    }
}
