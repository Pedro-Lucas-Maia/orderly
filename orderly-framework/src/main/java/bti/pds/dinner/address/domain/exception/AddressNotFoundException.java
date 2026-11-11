package bti.pds.dinner.address.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class AddressNotFoundException extends ResourceNotFoundException {
    public AddressNotFoundException(String message) {
        super("Address not found", message);
    }
}
