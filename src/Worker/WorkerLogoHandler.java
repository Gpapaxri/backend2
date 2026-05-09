package Worker;

import gr.softeng.distributedsystems.Entities.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
            // Determine file path (match the naming used when saving)
            Path logoFile = Paths.get("workerLogos/" + gameName);
            if (!Files.exists(logoFile)) {
                // Send empty or error – you can decide to close silently
                client.close();
                return;
            }

            byte[] fileBytes = Files.readAllBytes(logoFile);
            String base64 = Base64.getEncoder().encodeToString(fileBytes);
            byte[] utf8Bytes = base64.getBytes(StandardCharsets.UTF_8);

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeInt(utf8Bytes.length);
            out.write(utf8Bytes);
            out.flush();
            client.close();

            System.out.println("Sent Base64 as raw UTF‑8, size: " + utf8Bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
