package Worker;

import Entities.JSONReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Player;

public class WorkerMain {
    private static int port;
    static final String[] reducer = new String[2];
    static final String[] generator = new String[2];
    static ServerSocket worker;
    static final ArrayList<Game> games = new ArrayList<>();
    static final Map<String, Double> players_winnings_losses = new HashMap<>();
    static final ArrayList<Player> players = new ArrayList<>();
    //static final Map<String, Map<String,Integer>> ratings = new HashMap<>();

    public static void main(String[] args){

        Initialize(Integer.parseInt(args[0]));

        try {
            worker = new ServerSocket(port);
            while(true){
                Socket master = worker.accept();

                int w = Integer.parseInt(args[0]);
                System.out.println("Worker " + w + " received the request.");

                new WorkerCommunicationHandler(master).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void Initialize(int number){
        Map<String,String> map;

        try {
            map = JSONReader.readJson("config/configFile.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        port= Integer.parseInt(map.get("Worker" + number).split(" ")[1]);

        reducer[0] = map.get("Reducer").split(" ")[0];
        reducer[1] = map.get("Reducer").split(" ")[1];

        generator[0] = map.get("Generator").split(" ")[0];
        generator[1] = map.get("Generator").split(" ")[1];
    }

}
