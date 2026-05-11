package Worker;

import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkerLogoHandler extends Thread {
    Socket client;
    Message m;

    public WorkerLogoHandler(Socket client, Message m) {
        this.client = client;
        this.m = m;
    }

    @Override
    public void run() {
        String gameName = (String) m.getContent();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

            Path logoFile = Paths.get("workerLogos/" + gameName);
            if (!Files.exists(logoFile)) {
                oos.writeObject(new Message(MessageCode.GetLogo, null));
                oos.flush();
                client.close();
                return;
            }

            byte[] fileBytes = Files.readAllBytes(logoFile);

            oos.writeObject(new Message(MessageCode.GetLogo, fileBytes));
            oos.flush();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
