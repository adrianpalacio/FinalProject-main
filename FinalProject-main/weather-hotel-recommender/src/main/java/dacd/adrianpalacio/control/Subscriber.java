package dacd.adrianpalacio.control;

import dacd.adrianpalacio.control.exceptions.ReceiverException;

public interface Subscriber {
    void start() throws ReceiverException;
}
