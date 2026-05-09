package Entities;

import java.io.ObjectOutputStream;

public class PendingRequest {
    private final ObjectOutputStream oss;
    private final Object lock = new Object();
    private volatile boolean answered = false;

    public PendingRequest(ObjectOutputStream oss) {
        this.oss = oss;
    }

    public boolean isAnswered() {return answered;}

    public ObjectOutputStream getOss() {
        return oss;
    }

    public Object getLock() {
        return lock;
    }

    public void setAnswered(boolean answered) {this.answered = answered;}
}
