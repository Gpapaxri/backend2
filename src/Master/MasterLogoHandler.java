package Master;

import gr.softeng.distributedsystems.Entities.Message;

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
        String gameName = (String) m.getContent();
        int workerIndex = h(gameName) % MasterMain.num_workers;

        try {
            // Connect to the worker that owns this game
            Socket worker = new Socket(MasterMain.workers.get("Worker" + workerIndex)[0],  Integer.parseInt(MasterMain.workers.get("Worker" + workerIndex)[1]));

            // Forward the GetLogo request
            ObjectOutputStream oos = new ObjectOutputStream(worker.getOutputStream());
            oos.writeObject(m);
            oos.flush();

            // Read raw length + bytes from worker
            DataInputStream workerIn = new DataInputStream(worker.getInputStream());
            int length = workerIn.readInt();
            byte[] data = new byte[length];
            workerIn.readFully(data);

            // Forward to Android client
            DataOutputStream clientOut = new DataOutputStream(client.getOutputStream());
            clientOut.writeInt(length);
            clientOut.write(data);
            clientOut.flush();

            worker.close();
            client.close();
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
