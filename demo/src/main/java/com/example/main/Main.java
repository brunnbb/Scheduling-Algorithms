package com.example.main;

import com.example.computer.*;
import com.example.resources.*;

public class Main {
    public static void main(String[] args) {
        // Choose your json
        JsonParser jsonParser = new JsonParser("demo\\src\\main\\java\\com\\example\\jsons\\slideFcfs.json");
        Simulation simulation = jsonParser.readJson();
        Scheduler scheduler = new Scheduler(simulation);
        scheduler.start();
    }

}