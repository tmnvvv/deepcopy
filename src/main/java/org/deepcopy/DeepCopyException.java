package org.deepcopy;

public class DeepCopyException extends RuntimeException {

    public DeepCopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeepCopyException(String message) {
        super(message);
    }
}
