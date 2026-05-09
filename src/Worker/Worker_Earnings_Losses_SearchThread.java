package Worker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import gr.softeng.distributedsystems.Entities.Game;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class Worker_Earnings_Losses_SearchThread extends Thread{
    Socket client;
    Message m;

    public Worker_Earnings_Losses_SearchThread(Socket client, Message m) {
        this.client = client;
        this.m = m;
    }

    private Map<String, Game> mapSearch(String[] filters){
        Map<String, Game> games = new HashMap<>();

        synchronized (WorkerMain.games){

            if(filters[0].equalsIgnoreCase("all")){
                for(Game game: WorkerMain.games) {
                    if(game.isVisible()){
                        games.put(game.getGameName(), game);
                    }
                }
            }else{
                for(Game game: WorkerMain.games){
                    if(game.getStars() == Integer.parseInt(filters[0]) && game.getBetCategory().equals(filters[1]) && game.getRiskLevel().equals(filters[2]) && game.isVisible()){
                        games.put(game.getGameName(), game);
                    }
                }
            }
        }

        return games;
    }

    private Map<String, Double> mapEarnings_LossesPerGame(String provider){
        Map<String, Double> earnings = new HashMap<>();
        synchronized (WorkerMain.games){
            for(Game game: WorkerMain.games){
                if(game.getProviderName().equals(provider)){
                    earnings.put(game.getGameName(), game.getEarnings_losses());
                }
            }
        }

        return earnings;
    }

    private Map<String, Double> mapEarnings_LossesPerPlayer(String player){

        Map<String, Double> winnings = new HashMap<>();

        synchronized (WorkerMain.players_winnings_losses){
            winnings.put(player, WorkerMain.players_winnings_losses.get(player));
        }

        return winnings;
    }
    @Override
    public void run(){
        try {
            ObjectOutputStream oss = new ObjectOutputStream(client.getOutputStream());
            String[] contents = ((String) m.getContent()).split(" ");
            Message message;

            if(m.getCode() == MessageCode.SearchGames){
                Map<String, Game> games = mapSearch(contents);
                message = new Message(m.getCode(), games);

            }else if(m.getCode() == MessageCode.Earnings_LossesPerProvider){
                Map<String, Double> earnings = mapEarnings_LossesPerGame(contents[0]);
                message = new Message(m.getCode(), earnings);

            }else{
                Map<String, Double> player_winnings = mapEarnings_LossesPerPlayer(contents[0]);
                message = new Message(m.getCode(), player_winnings);
            }

            message.setMapId(m.getMapId());

            oss.writeObject(message);

            oss.flush();

            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
