package Master;

import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

import java.io.*;
import java.net.Socket;

public class MasterLogoHandler extends Thread {
    Socket client;
    Message m;

    public MasterLogoHandler(Socket client, Message m) {
        this.client = client;
        this.m = m;
    }

    @Override
    public void run() {
        try {

            String image = sentToWorker();

            ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());

            oss.writeUTF(image);

            oss.flush();

            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sentToWorker() {
        String gameName = (String) m.getContent();

        int socket = h(gameName) % MasterMain.num_workers;

        try {
            Socket worker = new Socket(MasterMain.workers.get("Worker"+socket)[0], Integer.parseInt(MasterMain.workers.get("Worker"+socket)[1]));

            ObjectOutputStream oss = new ObjectOutputStream(worker.getOutputStream());

            oss.writeObject(m);

            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(worker.getInputStream());

            String image = ois.readUTF();

            worker.close();

            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // same hash function as in MasterMain
    private int h(String name) {
        int hash = 0;
        for (int i = 0; i < name.length(); i++) {
            hash = 31 * hash + name.charAt(i);
        }
        return Math.abs(hash);
    }
}
