package org.uic.interpreter.exception;

public class TlbInterpreterException extends Exception {

    public TlbInterpreterException(String message) {
        super(message);
    }

    public TlbInterpreterException(Exception exception) {
        super(exception);
    }

}
