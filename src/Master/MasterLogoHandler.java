package Master;

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
            Message response = fetchFromWorker();

            ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());
            oss.writeObject(response);
            oss.flush();

            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Message fetchFromWorker() {
        String gameName = (String) m.getContent();
        int workerIndex = h(gameName) % MasterMain.num_workers;

        try {
            Socket worker = new Socket(
                MasterMain.workers.get("Worker" + workerIndex)[0],
                Integer.parseInt(MasterMain.workers.get("Worker" + workerIndex)[1])
            );

            ObjectOutputStream oss = new ObjectOutputStream(worker.getOutputStream());
            oss.writeObject(m);
            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(worker.getInputStream());
            Message response = (Message) ois.readObject();

            worker.close();
            return response;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new Message(MessageCode.GetLogo, null);
        }
    }

    private int h(String name) {
        int hash = 0;
        for (int i = 0; i < name.length(); i++) {
            hash = 31 * hash + name.charAt(i);
        }
        return Math.abs(hash);
    }
}
