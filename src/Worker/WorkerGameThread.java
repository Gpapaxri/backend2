package Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;
import gr.softeng.distributedsystems.Entities.Player;

public class WorkerGameThread extends Thread{

    Socket socket;
    Message m;

    public WorkerGameThread(Socket socket, Message m) {
        this.socket = socket;
        this.m = m;
    }

    private byte[] sha_256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private String toHEXString(byte[] hash){
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    private int sentTOSRG(String hashKey){
        try {
            Socket srg = new Socket(WorkerMain.generator[0], Integer.parseInt(WorkerMain.generator[1]));

            ObjectOutputStream oss = new ObjectOutputStream(srg.getOutputStream());

            oss.writeObject(new Message(MessageCode.PlayGame, hashKey));

            oss.flush();

            ObjectInputStream ois = new ObjectInputStream(srg.getInputStream());

            Message answer = (Message) ois.readObject();

            String[] numbers = ((String) answer.getContent()).split(" ");

            int luckyNumber = Integer.parseInt(numbers[0]);

            String hash = toHEXString(sha_256(luckyNumber + hashKey));

            if(!numbers[1].equals(hash)){
                return -1;
            }

            srg.close();

            return luckyNumber;
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(){
        try {
            ObjectOutputStream oss = new ObjectOutputStream(socket.getOutputStream());

            if(m.getCode() == MessageCode.AddGame) {

                Game game = (Game) m.getContent();

                byte[] logoBytes = m.getAttachment();

                if (logoBytes != null && logoBytes.length > 0) {

                    Path logoDir = Paths.get("workerLogos");
                    if (!Files.exists(logoDir)) Files.createDirectories(logoDir);
                    String fileName = game.getGameLogo();
                    Path logoFile = logoDir.resolve(fileName);
                    Files.write(logoFile, logoBytes);
                }

                synchronized (WorkerMain.games){
                    if(!WorkerMain.games.contains(game)){
                        WorkerMain.games.add(game);
                        oss.writeUTF("Successful addition.");
                    }else{
                        oss.writeUTF("The game already exists.");
                    }
                }
            }else if(m.getCode() == MessageCode.RemoveGame){

                String name = (String) m.getContent();

                String answer = "The game doesn't exist.";

                synchronized (WorkerMain.games) {
                    for (Game game : WorkerMain.games) {
                        if (game.getGameName().equals(name)) {
                            game.setVisible(false);
                            answer = "Successful removal";
                            break;
                        }
                    }
                }

                oss.writeUTF(answer);

            }else if(m.getCode() == MessageCode.ModifyGame){

                Game g = (Game) m.getContent();

                String answer = "The game doesn't exist.";

                byte[] logoBytes = m.getAttachment();

                if (logoBytes != null && logoBytes.length > 0) {

                    Path logoDir = Paths.get("workerLogos");
                    if (!Files.exists(logoDir)) Files.createDirectories(logoDir);
                    String fileName = g.getGameLogo();
                    Path logoFile = logoDir.resolve(fileName);
                    Files.write(logoFile, logoBytes);
                }

                synchronized (WorkerMain.games) {
                    for (Game game : WorkerMain.games) {
                        if (game.getGameName().equals(g.getGameName())) {
                            WorkerMain.games.remove(game);
                            g.setEarnings_losses(game.getEarnings_losses());
                            g.setVisible(game.isVisible());
                            WorkerMain.games.add(g);
                            answer = "Successful modification";
                            break;
                        }
                    }
                }

                oss.writeUTF(answer);
            }else if(m.getCode() == MessageCode.Rating) {

                String[] contents = (String[]) m.getContent();

                String gameName = contents[0];
                String player = contents[1];
                int stars = Integer.parseInt(contents[2]);
                int result = 0;

                synchronized (WorkerMain.games) {
                    for (Game game : WorkerMain.games) {
                        if (game.getGameName().equals(gameName)) {
                            double m = game.getRating() * game.getNoOfVotes() ;
                            m += stars;
                            System.out.println(m);
                            if(game.getRatings().containsKey(player)){
                                m -= game.getRatings().get(player);
                                game.getRatings().remove(player);
                                game.getRatings().put(player, stars);
                            }else {
                                game.getRatings().put(player, stars);
                                game.setNoOfVotes(game.getNoOfVotes() + 1);
                            }

                            game.setRating(m / game.getNoOfVotes());
                            game.setStars((int)Math.round(game.getRating()));
                            result = game.getStars();
                        }
                    }
                }

                oss.writeUTF("The stars are " + result);

            }else if(m.getCode() == MessageCode.UpdateWallet) {

                String username = ((String[]) m.getContent())[0];
                double amount = Double.parseDouble(((String[]) m.getContent())[1]);

                synchronized (WorkerMain.players) {
                    for(Player player : WorkerMain.players){
                        if(player.getUsername().equals(username)){
                            if(amount < 0){
                                player.getWallet().chargeWallet(-amount);
                            }else{
                                player.getWallet().rechargeWallet(amount);
                            }
                        }
                    }
                }

                oss.writeObject("Successful update");
            }else if(m.getCode() == MessageCode.SignIn) {

                Player player = (Player) m.getContent();
                String answer;

                synchronized (WorkerMain.players) {
                    if (!WorkerMain.players.contains(player)) {
                        WorkerMain.players.add(player);
                        answer = "Successful SignIn";
                    }else{
                        answer = "User already exists";
                    }
                }

                oss.writeObject(answer);
            }else if(m.getCode() == MessageCode.LogIn) {

                String username = ((String) m.getContent());

                Player p = new Player("", "");

                synchronized (WorkerMain.players) {
                    for(Player player : WorkerMain.players){
                        if(player.getUsername().equals(username)){
                            p = player;
                        }
                    }
                }

                oss.writeObject(p);
            }else {

                String[] contents = (String[]) m.getContent();

                Game g = new Game();

                synchronized (WorkerMain.games) {
                    for (Game game : WorkerMain.games) {
                        if (game.getGameName().equals(contents[0])) {
                            g = game;
                        }
                    }
                }

                int result = sentTOSRG(g.getHashKey());

                if(result == -1){
                    oss.writeDouble(-1.0);
                }else {
                    double amount;

                    synchronized (WorkerMain.games) {
                        if (result % 100 == 0) {
                            amount = Double.parseDouble(contents[2]) * g.getJackpot();
                        } else {
                            int position = result % 10;
                            amount = Double.parseDouble(contents[2]) * g.getMultipliers()[position];
                        }

                        g.setEarnings_losses(g.getEarnings_losses() - amount + Double.parseDouble(contents[2]));

                    }

                    synchronized (WorkerMain.players_winnings_losses){
                        if(WorkerMain.players_winnings_losses.get(contents[1]) == null){
                            WorkerMain.players_winnings_losses.put(contents[1], amount - Double.parseDouble(contents[2]));

                        }else{
                            double winnings = WorkerMain.players_winnings_losses.get(contents[1]);
                            WorkerMain.players_winnings_losses.remove(contents[1]);
                            WorkerMain.players_winnings_losses.put(contents[1], winnings + amount - Double.parseDouble(contents[2]));
                        }
                    }

                    oss.writeDouble(amount);
                }
            }
            oss.flush();

            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
