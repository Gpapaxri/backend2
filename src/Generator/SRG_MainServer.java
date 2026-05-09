package Generator;

import Entities.JSONReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class SRG_MainServer {
    private static int port;

    public static void main(String[] args) {

        Initialize();

        try{
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started at " + port);

            while(true){
                Socket worker = serverSocket.accept();
                System.out.println("Worker connected to SRG");
                new SRGRequest_Handler(worker).start();
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

        port = Integer.parseInt(map.get("Generator").split(" ")[1]);
    }
}