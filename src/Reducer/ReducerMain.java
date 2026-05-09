package Reducer;

import Entities.JSONReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ReducerMain {
    private static int port;
    static final String[] master = new String[2];
    static int num_workers;

    public static void main(String[] args) {

        Initialize();

        try {

            ServerSocket serverSocket = new ServerSocket(port);

            ReducerValueHolder valueHolder = new ReducerValueHolder();

            System.out.println("Reducer Started at "+ port);

            while (true) {
                Socket client = serverSocket.accept();
                new ActionsForReducerClients(client, valueHolder).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
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

        master[0] = map.get("Master").split(" ")[0];
        master[1] = map.get("Master").split(" ")[1];

        port = Integer.parseInt(map.get("Reducer").split(" ")[1]);
    }
}