package com.example.resources;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

    private int offset;
    @JsonProperty("computation_time")
    private int computationTime;
    @JsonProperty("period_time")
    private int periodTime;
    private int quantum;
    private int deadline;
    // Variables to help with calculations
    private int id;
    private int activations = 0;
    private List<Double> watList = new ArrayList<Double>();
    private List<Double> tatList = new ArrayList<Double>();
    private int numberOfLostDealines;
    private int relativeDeadline;
    private int computationTimeLeft;
    private int quantumLeft;

    public Task() {

    }

    public Task(int offset, int computationTime, int periodTime, int quantum, int deadline) {
        this.offset = offset;
        this.computationTime = computationTime;
        this.periodTime = periodTime;
        this.quantum = quantum;
        this.deadline = deadline;
        this.activations = 0;
        this.computationTimeLeft = computationTime;
        this.quantumLeft = quantum;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getComputationTime() {
        return computationTime;
    }

    public void setComputationTime(int computationTime) {
        this.computationTime = computationTime;
    }

    public int getPeriodTime() {
        return periodTime;
    }

    public void setPeriodTime(int periodTime) {
        this.periodTime = periodTime;
    }

    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivations() {
        return activations;
    }

    public void setActivations(int activations) {
        this.activations = activations;
    }

    public List<Double> getWatList() {
        return watList;
    }

    public void setWatList(List<Double> watList) {
        this.watList = watList;
    }

    public List<Double> getTatList() {
        return tatList;
    }

    public void setTatList(List<Double> tatList) {
        this.tatList = tatList;
    }

    public int getNumberOfLostDealines() {
        return numberOfLostDealines;
    }

    public void setNumberOfLostDealines(int numberOfLostDealines) {
        this.numberOfLostDealines = numberOfLostDealines;
    }

    public int getRelativeDeadline() {
        return relativeDeadline;
    }

    // Method to calculate relative deadlines for sorting tasks
    public void setRelativeDeadline(int time) {
        this.relativeDeadline = ((activations * deadline) - time - 1);
    }

    public int getComputationTimeLeft() {
        return computationTimeLeft;
    }

    public void setComputationTimeLeft(int computationTimeLeft) {
        this.computationTimeLeft = computationTimeLeft;
    }

    public int getQuantumLeft() {
        return quantumLeft;
    }

    public void setQuantumLeft(int quantumLeft) {
        this.quantumLeft = quantumLeft;
    }

    public String data() {
        double minWt = watList.get(0), maxWt = watList.get(0), totalWt = 0, totalTat = 0;
        for (Double x : watList) {
            if (x < minWt) {
                minWt = x;
            }
            if (x > maxWt) {
                maxWt = x;
            }
            totalWt += x;
        }
        for (Double y : tatList) {
            totalTat += y;
        }
        return "Task " + id + " - Activations: " + activations + " - Total WT: " + totalWt + " - Min WT: " + minWt
                + " - Max Wt: " + maxWt + " - Avg Wt: " + ((totalWt) / activations) + " - Total TAT: " + totalTat
                + " - Avg TAT: " + ((totalTat) / activations);
    }

    @Override
    public String toString() {
        return "Task " + id;
    }

}
