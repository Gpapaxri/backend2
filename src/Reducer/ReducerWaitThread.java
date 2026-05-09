package Reducer;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;


public class ReducerWaitThread extends Thread {
    ReducerValueHolder valueHolder;
    String id;
    MessageCode request;

    public ReducerWaitThread(ReducerValueHolder valueHolder, String id, MessageCode request) {
        this.valueHolder = valueHolder;
        this.id = id;
        this.request = request;
    }

    private Map<String, Object> reduceGames(List<Map<String, Object>> workerOutputs) {
        Map<String, Object> combined = new HashMap<>();
        for (Map<String, Object> out : workerOutputs) {
            if (out.isEmpty()) continue;
            combined.putAll(out);
        }
        return combined;
    }

    private String reduceProvider(List<Map<String, Object>> workerOutputs) {
        HashMap<String, Object> profitByGame = new HashMap<>();

        for (Map<String, Object> out : workerOutputs) {
            if (out.isEmpty()) continue;

            profitByGame.putAll(out);
        }

        double total = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : profitByGame.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            String formatted = formatProfit((Double) entry.getValue());
            sb.append(entry.getKey()).append("=").append(formatted);
            total += (Double) entry.getValue();
        }
        sb.append(", Total = ").append(formatProfit(total)).append(" FUN");
        return sb.toString();
    }

    private String reducePlayer(List<Map<String, Object>> workerOutputs) {
        double profit = 0;

        for (Map<String, Object> out : workerOutputs) {
            if (out.isEmpty()) continue;

            for(String key : out.keySet()){

                if(out.get(key) != null){
                    profit += (Double) out.get(key);
                }
            }
        }

        return "Total Profit/Loss : " + formatProfit(profit);
    }

    private String formatProfit(double value) {
        if (value >= 0) return String.format("+%.1f", value);
        return String.format("%.1f", value);
    }

   
    @Override
    public void run() {
        synchronized (valueHolder) {
            while (!valueHolder.allOK(id)) {
                System.out.println("Ο Reducer περιμένει δεδομένα...");
                try {
                    valueHolder.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            List<Map<String, Object>> data = valueHolder.workerOuts.get(id);
            Message message = new Message(MessageCode.ReducerReply_Winnings_Losses, "");
            message.setMapId(Integer.parseInt(id));


            switch (request) {
                case MessageCode.SearchGames:
                    message.setContent(reduceGames(data));
                    message.setCode(MessageCode.ReducerReply_Games);
                    break;
                case MessageCode.Earnings_LossesPerProvider:
                    message.setContent(reduceProvider(data));
                    break;
                case MessageCode.Earnings_LossesPerPlayer:
                    message.setContent(reducePlayer(data));
                    break;
            }

            try  {
                Socket masterSocket = new Socket(ReducerMain.master[0], Integer.parseInt(ReducerMain.master[1]));

                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());

                out.writeObject(message);

                out.flush();

                masterSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            valueHolder.workerOuts.remove(id);
            valueHolder.numOfWorkersAnswered.remove(id);
            valueHolder.numOfWorkersExpected.remove(id);
        }
    }
}