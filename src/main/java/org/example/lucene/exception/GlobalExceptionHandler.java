package org.example.lucene.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {BookLuceneException.class})
    public String bookLuceneException(BookLuceneException e) {
        LOGGER.error(e.getMessage(), e);
        return "error";
    }
}
