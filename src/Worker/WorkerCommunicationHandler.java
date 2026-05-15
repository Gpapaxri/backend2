package Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class WorkerCommunicationHandler extends Thread{
    Socket client;

    public WorkerCommunicationHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run(){
        try {
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            Message m = (Message) ois.readObject();

            if(m.getCode() == MessageCode.AddGame || m.getCode() == MessageCode.ModifyGame || m.getCode() == MessageCode.RemoveGame || m.getCode() == MessageCode.PlayGame || m.getCode() == MessageCode.Rating || m.getCode() == MessageCode.LogIn || m.getCode() == MessageCode.SignIn || m.getCode() == MessageCode.UpdateWallet){
                new WorkerGameThread(client, m).start();

            }else if (m.getCode() == MessageCode.GetLogo) {
                new WorkerLogoHandler(client, m).start();

            }else {
                new Worker_Earnings_Losses_SearchThread(new Socket(WorkerMain.reducer[0], Integer.parseInt(WorkerMain.reducer[1])), m).start();
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
