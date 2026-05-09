package Master;

import Entities.PendingRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class MasterCommunicationHandler extends Thread{
    Socket client;

    public MasterCommunicationHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run(){
        try {
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            Message m = (Message) ois.readObject();

            if(m.getCode() == MessageCode.AddGame || m.getCode() == MessageCode.ModifyGame || m.getCode() == MessageCode.RemoveGame || m.getCode() == MessageCode.PlayGame || m.getCode() == MessageCode.Rating){
                new MasterGameHandlingThread(client, m).start();
            }else if(m.getCode() == MessageCode.LogIn || m.getCode() == MessageCode.SignIn || m.getCode() == MessageCode.UpdateWallet){
                new MasterUserHandlingThread(client, m).start();
            }else if (m.getCode() == MessageCode.GetLogo) { /// //////////////
                new MasterLogoHandler(client, m).start(); ////////////////////////
            }else if(m.getCode() == MessageCode.ReducerReply_Winnings_Losses || (m.getCode() == MessageCode.ReducerReply_Games)){
                int mapId = m.getMapId();

                PendingRequest request;

                synchronized (MasterMain.requests){
                    request = MasterMain.requests.remove(mapId);
                }

                if(request != null){
                    synchronized (request.getLock()){
                            request.setAnswered(true);
                        try{
                            if(m.getCode() == MessageCode.ReducerReply_Games){
                                request.getOss().writeObject(m.getContent());
                            }else {
                                String result = (String) m.getContent();

                                request.getOss().writeUTF(result);
                            }

                            request.getOss().flush();

                        }catch (IOException e){
                            throw new RuntimeException(e);
                        }finally {
                            request.getLock().notify();
                        }
                    }
                }
            }else {

                synchronized(MasterMain.class){
                    MasterMain.mapID++;
                    m.setMapId(MasterMain.mapID);
                }

                int mapId = m.getMapId();

                ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());

                PendingRequest request = new PendingRequest(oss);

                synchronized (MasterMain.requests){
                    MasterMain.requests.put(mapId, request);
                }

                new MasterEarnings_Losses_SearchThread(m).start();

                synchronized (request.getLock()){
                    if(!request.isAnswered()){
                        try{
                            request.getLock().wait();
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                client.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
