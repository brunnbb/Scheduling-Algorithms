package com.example.main;

import com.example.computer.*;
import com.example.resources.*;

public class Main {
    public static void main(String[] args) {
        JsonParser jsonParser = new JsonParser();
        Simulation simulation = jsonParser.readJson("demo\\src\\main\\java\\com\\example\\jsons\\slideFailedRm.json");
        Scheduler scheduler = new Scheduler(simulation);
        scheduler.start();
    }

}