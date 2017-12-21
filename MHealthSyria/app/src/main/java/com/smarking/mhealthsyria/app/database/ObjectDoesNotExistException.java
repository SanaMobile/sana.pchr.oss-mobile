package com.smarking.mhealthsyria.app.database;

/**
 * Exception to be thrown if a query returns zero results
 * and the relationship declares one should.
 */
public class ObjectDoesNotExistException extends Exception {

    public ObjectDoesNotExistException(String message, Exception e) {
        super(message, e);
    }

    public ObjectDoesNotExistException(String message) {
        super(message);
    }

    public ObjectDoesNotExistException() {
        super();
    }
}
