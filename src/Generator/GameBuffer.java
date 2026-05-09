package Generator;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class GameBuffer {

    private LinkedList<RandomNumber> buffer;
    private SRG srg;
    private static final int buffer_size = 100;


    public GameBuffer(String hashKey){
        this.buffer = new LinkedList<>();
        this.srg = new SRG(hashKey, this);
        this.srg.start();
    }

    public void add(RandomNumber random_number) {
        synchronized (buffer) {
            while (buffer.size() >= buffer_size) {
                try {
                    buffer.wait();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            buffer.offer(random_number);
            buffer.notifyAll();
        }
    }

    public RandomNumber get_number() throws InterruptedException {
        synchronized (buffer) {
            while (buffer.isEmpty()) {
                buffer.wait();
            }
            return buffer.poll();
        }
    }
}