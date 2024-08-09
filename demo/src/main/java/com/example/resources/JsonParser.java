package com.example.resources;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonParser {

    public JsonParser() {

    }

    public Simulation readJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {

            Simulation simulation = objectMapper.readValue(new File(filePath), Simulation.class);

            int id = 1;
            for (Task task : simulation.getTasks()) {
                task.setId(id++);
                task.setComputationTimeLeft(task.getComputationTime());
                task.setQuantumLeft(task.getQuantum());
            }

            return simulation;

        } catch (IOException e) {

            System.out.println("This json was not found! ");
            return null;
        }
    }
}
