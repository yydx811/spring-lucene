package org.example.lucene.exception;

public class BookLuceneException extends RuntimeException {

    public BookLuceneException() {
    }

    public BookLuceneException(String message) {
        super(message);
    }

    public BookLuceneException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookLuceneException(Throwable cause) {
        super(cause);
    }
}
