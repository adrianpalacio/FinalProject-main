package dacd.adrianpalacio.control;

import dacd.adrianpalacio.control.exceptions.ReceiverException;

import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);

        try {
            FileEventStoreBuilder fileEventStoreBuilder = new FileEventStoreBuilder(args[0]);
            TopicSubscriber datalakeBuilder = new TopicSubscriber(fileEventStoreBuilder, "prediction.Weather", "information.Hotel", semaphore);
            datalakeBuilder.start();

        } catch (ReceiverException e) {
            e.printStackTrace();
        }
    }
}
