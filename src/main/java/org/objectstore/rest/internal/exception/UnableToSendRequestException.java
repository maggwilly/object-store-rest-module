package org.objectstore.rest.internal.exception;

import org.mule.runtime.api.store.ObjectStoreException;

public class UnableToSendRequestException extends RuntimeException {
    public UnableToSendRequestException(String message, ObjectStoreException var7) {
        super(message, var7);
    }
}
