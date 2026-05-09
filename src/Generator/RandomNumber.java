package Generator;

import java.io.Serializable;

public class RandomNumber implements Serializable {

    private int number;
    private byte[] secret;

    public RandomNumber(int number, byte[] secret) {
        this.number = number;
        this.secret = secret;
    }

    public int getNumber() {
        return number;
    }

    public byte[] getHash() {return secret;}
}