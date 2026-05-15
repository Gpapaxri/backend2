package Ui;

import Entities.JSONReader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class ConsoleApp {
    private static final String[] master_details = new String[2];

    public static void main(String[] args){
        Scanner in = new Scanner(System.in);

        Initialize();

        while(true){
            System.out.println("1.Add a game to the system");
            System.out.println("2.Remove a game from the system");
            System.out.println("3.Modify a game from the system");
            System.out.println("4.Total Earnings/Losses per game");
            System.out.println("5.Total Earnings/Losses per player");
            System.out.println("0.exit");
            System.out.println("Choose from the above by putting the corresponding number (0-5): ");
            int answer = Integer.parseInt(in.nextLine());

            while(answer > 5 || answer < 0){
                System.out.println("Wrong input!!! Please put a number between 0 and 5.");
                answer = Integer.parseInt(in.nextLine());
            }

            if(answer == 0){
                break;
            }

            if(answer == 1){
                System.out.println("Give the name of the json file");
                String game = in.nextLine();
                Map<String,String> map;

                try {
                    map = JSONReader.readJson("games/"+game+".json");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                byte[] logoBytes;

                try {
                    logoBytes = Files.readAllBytes(Paths.get(map.get("GameLogo")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String logo = Paths.get(map.get("GameLogo")).getFileName().toString();

                Game g = new Game(map.get("GameName"), map.get("ProviderName"), Integer.parseInt(map.get("Stars")), Integer.parseInt(map.get("NoOfVotes")), logo, Double.parseDouble(map.get("MinBet")), Double.parseDouble(map.get("MaxBet")), map.get("RiskLevel"), map.get("HashKey"));

                Message m = new Message(MessageCode.AddGame, g);

                m.setAttachment(logoBytes);

                sendMessage(m);

            }else if(answer == 2){
                System.out.println("Give the name of the game");
                String game = in.nextLine();

                Message m = new Message(MessageCode.RemoveGame,game);
                sendMessage(m);

            }else if(answer == 3){
                System.out.println("Give the path of the json file for the game to be modified");
                String game = in.nextLine();

                Map<String,String> map;

                try {
                    map = JSONReader.readJson("games/"+game+".json");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                byte[] logoBytes;

                try {
                    logoBytes = Files.readAllBytes(Paths.get(map.get("GameLogo")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String logo = Paths.get(map.get("GameLogo")).getFileName().toString();

                Game g = new Game(map.get("GameName"), map.get("ProviderName"), Integer.parseInt(map.get("Stars")), Integer.parseInt(map.get("NoOfVotes")),logo, Double.parseDouble(map.get("MinBet")), Double.parseDouble(map.get("MaxBet")), map.get("RiskLevel"), map.get("HashKey"));

                Message m = new Message(MessageCode.ModifyGame,g);

                m.setAttachment(logoBytes);

                sendMessage(m);

            }else if(answer == 4){
                System.out.println("Give the name of the provider");
                String provider = in.nextLine();

                Message m = new Message(MessageCode.Earnings_LossesPerProvider,provider);
                sendMessage(m);

            }else {
                System.out.println("Give the username of the player");
                String username = in.nextLine();

                Message m = new Message(MessageCode.Earnings_LossesPerPlayer, username);
                sendMessage(m);
            }
        }
    }

    private static void sendMessage(Message m){
        try {
            Socket master = new Socket(master_details[0], Integer.parseInt(master_details[1]));
            ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

            oss.writeObject(m);

            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

            String content = (String) ois.readObject();

            master.close();

            System.out.println(content);
        } catch (IOException | ClassNotFoundException e) {
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

        master_details[0] =  map.get("Master").split(" ")[0];
        master_details[1] =  map.get("Master").split(" ")[1];

    }
}
