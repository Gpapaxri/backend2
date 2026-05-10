package Worker;

import gr.softeng.distributedsystems.Entities.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

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
            Path logoFile = Paths.get("workerLogos/" + gameName);

            if (!Files.exists(logoFile)) {
                client.close();
                return;
            }

            byte[] fileBytes = Files.readAllBytes(logoFile);
            String base64 = Base64.getEncoder().encodeToString(fileBytes);

            ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());
            oss.writeUTF(base64);
            oss.flush();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
