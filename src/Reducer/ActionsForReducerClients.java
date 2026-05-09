package Reducer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import gr.softeng.distributedsystems.Entities.Message;
import gr.softeng.distributedsystems.Entities.MessageCode;

public class ActionsForReducerClients extends Thread {
    Socket connection;
    ReducerValueHolder valueHolder;

    public ActionsForReducerClients(Socket connection, ReducerValueHolder valueHolder) {
        this.connection = connection;
        this.valueHolder = valueHolder;
    }

    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

            Message input = (Message) in.readObject();

            String id = String.valueOf(input.getMapId());
            MessageCode request = input.getCode();

            int expectedWorkers = ReducerMain.num_workers;
            Map<String, Object> mappedData = (Map<String, Object>) input.getContent();

            synchronized (valueHolder) {
                if (!valueHolder.numOfWorkersExpected.containsKey(id)) {
                    valueHolder.numOfWorkersExpected.put(id, expectedWorkers);
                    valueHolder.numOfWorkersAnswered.put(id, 0);
                    valueHolder.workerOuts.put(id, new ArrayList<>());

                    new ReducerWaitThread(valueHolder, id, request).start();
                }

                valueHolder.workerOuts.get(id).add(mappedData);
                int count = valueHolder.numOfWorkersAnswered.get(id);
                valueHolder.numOfWorkersAnswered.put(id, count+1);

                valueHolder.notifyAll();
            }

            connection.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}