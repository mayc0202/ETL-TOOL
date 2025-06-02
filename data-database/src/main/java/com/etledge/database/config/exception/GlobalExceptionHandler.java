package com.etledge.database.config.exception;

import com.etledge.common.result.RtnData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Global Exception Processor
 *
 * @author mayc
 * @since 2025-05-22 00:00:40
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Default system error prompt message
    private static final String SYSTEM_ERROR_MSG = "System exception, please try again later!";

    // Parameter verification error prefix
    private static final String VALIDATION_ERROR_PREFIX = "Parameter verification failed: ";

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public RtnData handleException(Throwable e, HttpServletResponse response) {
        logException(e);

        if (e instanceof ETLException) {
            return RtnData.fail(e.getMessage());
        } else if (e instanceof BindException) {
            return handleBindException((BindException) e);
        } else if (e instanceof MethodArgumentNotValidException) {
            return handleMethodArgumentNotValidException((MethodArgumentNotValidException) e);
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            return RtnData.fail("Unsupported request method: " + e.getMessage());
        } else if (e instanceof MissingServletRequestParameterException
                || e instanceof ServletRequestBindingException
                || e instanceof MissingServletRequestPartException) {
            return RtnData.fail("Required parameter missing: " + e.getMessage());
        } else if (e instanceof HttpMessageNotReadableException) {
            return RtnData.fail("Request body parsing failed: " + e.getMessage());
        } else if (e instanceof UnsupportedEncodingException) {
            return RtnData.fail("Encoding not supported: " + e.getMessage());
        } else if (e instanceof NullPointerException) {
            return RtnData.fail("Null value anomaly: " + e.getMessage());
        } else if (e instanceof SQLException) {
            return RtnData.fail("SQLException : " + e.getMessage());
        } else if (e instanceof DataIntegrityViolationException) {
            return RtnData.fail("Data Integrity Violation Exception : " + e.getMessage());
        } else {
            return RtnData.fail(SYSTEM_ERROR_MSG);
        }
    }

    /**
     * Handle parameter binding exceptions
     */
    private RtnData handleBindException(BindException ex) {
        List<FieldError> fieldErrors = ex.getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return RtnData.fail(VALIDATION_ERROR_PREFIX + errorMsg);
    }

    /**
     * Processing method parameter verification exception
     */
    private RtnData handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        if (!bindingResult.hasErrors()) {
            logger.warn("No binding result or errors found in MethodArgumentNotValidException");
            return RtnData.fail(VALIDATION_ERROR_PREFIX + "parameter verification failed");
        }

        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return RtnData.fail(VALIDATION_ERROR_PREFIX + errorMsg);
    }

    /**
     * Record abnormal logs
     */
    private void logException(Throwable e) {
        if (e instanceof ETLException) {
            logger.warn("Business exception: {}", e.getMessage());
        } else if (e instanceof BindException || e instanceof MethodArgumentNotValidException) {
            logger.warn("Validation exception: {}", e.getMessage());
        } else {
            logger.error("System exception occurred", e);
        }
    }
}