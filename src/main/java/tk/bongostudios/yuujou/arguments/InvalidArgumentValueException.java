package tk.bongostudios.yuujou.arguments;

import javax.management.InvalidAttributeValueException;

public class InvalidArgumentValueException extends InvalidAttributeValueException {

    private static final long serialVersionUID = 584937491037597428L;

    public InvalidArgumentValueException() {
        super();
    }

    public InvalidArgumentValueException(String msg) {
        super(msg);
    }
}