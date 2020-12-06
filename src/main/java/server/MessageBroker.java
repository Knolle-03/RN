package server;

import lombok.SneakyThrows;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageBroker {

    private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<>(100);
    private boolean serverIsUp = true;


    public void put(String message) {
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String get() throws InterruptedException {
        return messages.poll(1, TimeUnit.SECONDS);
    }



//    public class Broker
//    {
//        public ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(100);
//        public Boolean continueProducing = Boolean.TRUE;
//
//        public void put(Integer data) throws InterruptedException
//        {
//            this.queue.put(data);
//        }
//
//        public Integer get() throws InterruptedException
//        {
//            return this.queue.poll(1, TimeUnit.SECONDS);
//        }
//    }


}
