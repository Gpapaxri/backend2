package Master;

import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;
import gr.softeng.distributedsystems.Entities.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MasterUserHandlingThread extends Thread{
    Socket client;
    Message m;

    public MasterUserHandlingThread(Socket client, Message m) {
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
            ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());

            if(m.getCode() == MessageCode.SignIn || m.getCode() == MessageCode.UpdateWallet){
                String c = (String) sentToWorker();
                oss.writeObject(c);
            }else {
                Player player = (Player) sentToWorker();
                oss.writeObject(player);
            }

            oss.flush();

            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object sentToWorker() {
        String username;

        if(m.getCode() == MessageCode.SignIn){
            username = ((Player) m.getContent()).getUsername();
        }else if(m.getCode() == MessageCode.LogIn){
            username = (String) m.getContent();
        }else {
            username = ((String[]) m.getContent())[0];
        }

        int socket = h(username) % MasterMain.num_workers;

        try {
            Socket worker = new Socket(MasterMain.workers.get("Worker"+socket)[0], Integer.parseInt(MasterMain.workers.get("Worker"+socket)[1]));

            ObjectOutputStream oss = new ObjectOutputStream(worker.getOutputStream());

            oss.writeObject(m);

            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(worker.getInputStream());

            Object answer = ois.readObject();

            worker.close();

            return answer;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
