package Generator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class SRGRequest_Handler extends Thread{

    private Socket worker;

    public SRGRequest_Handler(Socket worker) {
        this.worker = worker;
    }

    private String toHEXString(byte[] hash){
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    @Override
    public void run() {

        try {
            ObjectInputStream workerInput = new ObjectInputStream(worker.getInputStream());
            String HashKey = (String)((Message) workerInput.readObject()).getContent();

            RandomNumber number = SRGBuffer_Handler.get_RandomNumber(HashKey);

            Message number_hash = new Message(MessageCode.PlayGame, number.getNumber() + " " + toHEXString(number.getHash()));

            ObjectOutputStream workerOutput = new ObjectOutputStream(worker.getOutputStream());

            workerOutput.writeObject(number_hash);

            workerOutput.flush();

            worker.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}