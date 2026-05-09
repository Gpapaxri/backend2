package Generator;

import java.util.HashMap;

public class SRGBuffer_Handler {

    private static HashMap<String, GameBuffer> gameBuffers = new HashMap<>();

    public static RandomNumber get_RandomNumber(String hashkey) throws InterruptedException {

        GameBuffer gameBuffer;

        synchronized (gameBuffers) {
            gameBuffer = gameBuffers.get(hashkey);
            if (gameBuffer == null) {
                gameBuffer = new GameBuffer(hashkey);
                gameBuffers.put(hashkey, gameBuffer);
                System.out.println("Created new buffer for game with the hashKey: " + hashkey);
            }
        }

        return gameBuffer.get_number();
    }
}