package com.shopcart.dto.response;

import java.time.LocalDateTime;

public class ErrorResponse {
    public LocalDateTime timestamp;
    public Integer status;
    public String error;
    public String message;
    public String path;
    
    public ErrorResponse() {}
    
    public ErrorResponse(LocalDateTime timestamp, Integer status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
