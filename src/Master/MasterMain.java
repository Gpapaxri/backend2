package Master;

import Entities.JSONReader;
import Entities.PendingRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class MasterMain {
    private static int port;
    static int mapID;
    static final Map<Integer, PendingRequest> requests = new HashMap<>();
    static int num_workers;
    final static Map<String, String[]> workers = new HashMap<>();

    public static void main(String[] args){

        Initialize();

        try{
            ServerSocket master = new ServerSocket(port);
            System.out.println("Server started at " + port);

            while(true) {
                Socket client = master.accept();
                new MasterCommunicationHandler(client).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void Initialize(){
        Map<String,String> map;

        try {
            map = JSONReader.readJson("config/configFile.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        num_workers = Integer.parseInt(map.get("NumOfWorkers"));

        for(int i = 0; i < num_workers; i++){
            workers.put("Worker" + i, map.get("Worker" + i).split(" "));
        }

        for(int i = 0; i < 5; i++){

            Map<String, String> game;

            try {
                game = JSONReader.readJson("games/game"+i+".json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] logoBytes;

            try {
                logoBytes = Files.readAllBytes(Paths.get(game.get("GameLogo")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String logo = Paths.get(game.get("GameLogo")).getFileName().toString();

            Game g = new Game(game.get("GameName"), game.get("ProviderName"), Integer.parseInt(game.get("Stars")), Integer.parseInt(game.get("NoOfVotes")), logo, Double.parseDouble(game.get("MinBet")), Double.parseDouble(game.get("MaxBet")), game.get("RiskLevel"), game.get("HashKey"));

            Message m = new Message(MessageCode.AddGame,g);

            m.setAttachment(logoBytes);

            int socket = h(g.getGameName()) % num_workers;

            try {
                Socket worker = new Socket(workers.get("Worker"+socket)[0], Integer.parseInt(workers.get("Worker"+socket)[1]));

                ObjectOutputStream oss = new ObjectOutputStream(worker.getOutputStream());

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(worker.getInputStream());

                String answer = ois.readUTF();

                worker.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        port = Integer.parseInt(map.get("Master").split(" ")[1]);
    }

    private static int h(String name) {
        int hash = 0;
        for (int i = 0; i < name.length(); i++) {
            hash = 31 * hash + name.charAt(i);
        }
        return Math.abs(hash);
    }
}
