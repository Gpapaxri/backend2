package Master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class MasterGameHandlingThread extends Thread{

    Socket client;
    Message m;

    public MasterGameHandlingThread(Socket client, Message m) {
        this.client = client;
        this.m = m;
    }

    private int h(String name) {
        int hash = 0;
        for (int i = 0; i < name.length(); i++) {
            hash = 31 * hash + name.charAt(i);
        }
        return Math.abs(hash);
    }

    @Override
    public void run(){
        try {
            String c = sentToWorker();

            ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());

            if(m.getCode() == MessageCode.PlayGame){
                oss.writeObject(Double.parseDouble(c));
            }else{
                oss.writeObject(c);
            }

            oss.flush();

            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sentToWorker() {
        String gameName;

        if(m.getCode() == MessageCode.PlayGame || m.getCode() == MessageCode.Rating){
            gameName = ((String[]) m.getContent())[0];
        }else if(m.getCode() == MessageCode.RemoveGame){
            gameName = (String) m.getContent();
        }else {
            gameName = ((Game) m.getContent()).getGameName();
        }

        int socket = h(gameName) % MasterMain.num_workers;

        try {
            Socket worker = new Socket(MasterMain.workers.get("Worker"+socket)[0], Integer.parseInt(MasterMain.workers.get("Worker"+socket)[1]));

            ObjectOutputStream oss = new ObjectOutputStream(worker.getOutputStream());

            oss.writeObject(m);

            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(worker.getInputStream());

            String answer;

            if(m.getCode() == MessageCode.PlayGame){
                answer = String.valueOf(ois.readDouble());
            }else{
                answer = ois.readUTF();
            }

            worker.close();

            return answer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
