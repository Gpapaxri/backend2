package Reducer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReducerValueHolder {
    HashMap<String, List<Map<String, Object>>> workerOuts = new HashMap<>();
    HashMap<String, Integer> numOfWorkersAnswered = new HashMap<>();
    HashMap<String, Integer> numOfWorkersExpected = new HashMap<>();

    public boolean allOK(String id) {
        if (!numOfWorkersExpected.containsKey(id)) return false;
        return numOfWorkersAnswered.get(id) >= numOfWorkersExpected.get(id);
    }
}