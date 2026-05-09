package Ui;

import Entities.JSONReader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class DummyApp {

    public static void main(String[] args){
        Scanner in = new Scanner(System.in);

        String[] master_details = Initialize();

        System.out.println("Username: ");
        String username = in.nextLine();

        while(true){
            System.out.println("Choose the filters:");
            System.out.println("How Many Stars(1-5):");

            int stars = Integer.parseInt(in.nextLine());
            while (stars > 5 || stars < 1){
                System.out.println("Wrong input!!! Please put a number between 1 and 5 ");
                stars = Integer.parseInt(in.nextLine());
            }

            System.out.println("How much do you want to bet($/$$/$$$):");
            String bid = in.nextLine();
            while (!bid.equals("$") && !bid.equals("$$") && !bid.equals("$$$")){
                System.out.println("Wrong input!!! Please put $ or $$ or $$$.");
                bid = in.nextLine();
            }

            System.out.println("How much risk do you want(Low/Medium/High):");
            String risk = in.nextLine();
            while (!risk.equals("Low") && !risk.equals("Medium") && !risk.equals("High")){
                System.out.println("Wrong input!!! Please put Low or Medium or High.");
                risk = in.nextLine();
            }

            Map<String, Object> res;

            try {
                Socket master = new Socket(master_details[0], Integer.parseInt(master_details[1]));

                ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());
                String filters = stars + " " + bid + " " + risk;
                Message m = new Message(MessageCode.SearchGames,filters);

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                res = (Map<String, Object>) ois.readObject();

                if(res.isEmpty()){
                    System.out.println("There aren't any games that correspond to the filters. Please put new filters ");
                    continue;
                }

                System.out.println("The available games that correspond to the filters: ");

                for(String key : res.keySet()){
                    Game g = (Game) res.get(key);
                    System.out.println("Game: " + g.getGameName());
                    System.out.println("Minimum bet: " + g.getMinBet());
                    System.out.println("Maximum bet: " + g.getMaxBet());
                    System.out.println("Risk Level: " + g.getRiskLevel());
                    System.out.println("Jackpot: " + g.getJackpot());
                }

                master.close();

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Give name of the game: ");
            String gameName = in.nextLine();
            while(!res.containsKey(gameName)){
                System.out.println("Wrong input!!! Please put a name that corresponds to the list.");
                gameName = in.nextLine();
            }

            System.out.println("Give the bet amount: ");
            String bet = in.nextLine();
            while(Double.parseDouble(bet) < ((Game) res.get(gameName)).getMinBet() || Double.parseDouble(bet) > ((Game) res.get(gameName)).getMaxBet()){
                System.out.println("Wrong input!!! Please put a bid that is between " + ((Game) res.get(gameName)).getMinBet() + " and " + ((Game) res.get(gameName)).getMaxBet());
                bet = in.nextLine();
            }

            double result;

            try {
                Socket master = new Socket(master_details[0], Integer.parseInt(master_details[1]));

                ObjectOutputStream oss = new ObjectOutputStream(master.getOutputStream());

                String[] nub = {gameName, username, bet};

                Message m = new Message(MessageCode.PlayGame, nub);

                oss.writeObject(m);

                oss.flush();

                ObjectInputStream ois = new ObjectInputStream(master.getInputStream());

                result = (double) ois.readObject();

                master.close();

                System.out.println(result);

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if(result == -1){
                continue;
            }

            System.out.println("Do you want to play again(yes/no):");

            String answer = in.nextLine();
            while(!answer.equalsIgnoreCase("no") && !answer.equalsIgnoreCase("yes")){
                System.out.println("Wrong input!!! Please put yes or no.");
                answer = in.nextLine();
            }

            if(answer.equals("no")){
                break;
            }
        }


    }

    private static String[] Initialize(){
        Map<String,String> map;

        try {
            map = JSONReader.readJson("config/configFile.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] master = new String[2];

        master[0] =  map.get("Master").split(" ")[0];
        master[1] =  map.get("Master").split(" ")[1];

        return master;
    }
}
