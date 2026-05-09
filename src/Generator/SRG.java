package Generator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SRG extends Thread{

    private String hashkey;
    private GameBuffer buffer;
    private volatile boolean running = true;
    private SecureRandom secureRandom;

    public SRG(String hashkey,GameBuffer buffer){
        this.hashkey = hashkey;
        this.buffer = buffer;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public void run() {
        System.out.println("Producer started for the game with the hashKey: " + hashkey);

        while (running) {
            try {

                int random_number = secureRandom.nextInt(100);

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String text = random_number + hashkey;

                byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
                RandomNumber randomNum = new RandomNumber(random_number, hash);

                buffer.add(randomNum);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Producer stopped for game with the hashKey: " + hashkey);
    }
}
