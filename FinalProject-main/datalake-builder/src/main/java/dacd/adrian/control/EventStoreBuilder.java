package dacd.adrianpalacio.control;

import dacd.adrianpalacio.control.exceptions.SaveException;

public interface EventStoreBuilder {
    void save(String message, String topic) throws SaveException;
}
