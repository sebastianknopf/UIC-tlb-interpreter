package org.uic.interpreter.exception;

public class TlbConstraintException extends Exception {

    public TlbConstraintException(Exception exception) {
        super(exception);
    }

    public TlbConstraintException(String message) {
        super(message);
    }
}
