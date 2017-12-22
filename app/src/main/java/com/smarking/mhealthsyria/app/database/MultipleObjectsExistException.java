package com.smarking.mhealthsyria.app.database;

/**
 * Exception to be thrown if a query returns more than one result
 * where the relationship declares that only one should be.
 */
public class MultipleObjectsExistException extends Exception {
    public MultipleObjectsExistException(String message, Exception e) {
        super(message, e);
    }

    public MultipleObjectsExistException(String message) {
        super(message);
    }

    public MultipleObjectsExistException() {
        super();
    }
}
