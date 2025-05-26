package com.ds.etl.upms.config.exception;

/**
 * @Author: yc
 * @CreateTime: 2025-05-17
 * @Description:
 * @Version: 1.0
 */
public class ETLException extends RuntimeException {

    private String errorCode;
    private String message;

    public ETLException() {
    }

    public ETLException(Throwable e) {
        super(e);
    }

    public ETLException(String message) {
        this.message = message;
    }

    public ETLException(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.message;
    }
}