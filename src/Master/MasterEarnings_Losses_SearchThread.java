package Master;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import gr.softeng.distributedsystems.Entities.Message;


public class MasterEarnings_Losses_SearchThread extends Thread{
    Message m;

    public MasterEarnings_Losses_SearchThread(Message m) {
        this.m = m;
    }

    @Override
    public void run(){
        try {
            for(int i = 0; i < MasterMain.num_workers; i++){
                Socket worker = new Socket(MasterMain.workers.get("Worker"+i)[0], Integer.parseInt(MasterMain.workers.get("Worker"+i)[1]));

                ObjectOutputStream oss = new ObjectOutputStream(worker.getOutputStream());

                oss.writeObject(m);

                oss.flush();

                worker.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
