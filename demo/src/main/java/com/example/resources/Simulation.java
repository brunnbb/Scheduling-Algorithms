package com.example.resources;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Simulation {
    @JsonProperty("simulation_time")
    private int simulationTime;
    @JsonProperty("scheduler_name")
    private String schedulerName;
    @JsonProperty("tasks_number")
    private int tasksNumber;
    private List<Task> tasks;
    private double cpuUtilization = 0;

    public Simulation() {

    }

    public Simulation(int simulationTime, String schedulerName, int tasksNumber, List<Task> tasks) {
        this.simulationTime = simulationTime;
        this.schedulerName = schedulerName;
        this.tasksNumber = tasksNumber;
        this.tasks = tasks;

    }

    public int getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(int simulationTime) {
        this.simulationTime = simulationTime;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public int getTasksNumber() {
        return tasksNumber;
    }

    public void setTasksNumber(int tasksNumber) {
        this.tasksNumber = tasksNumber;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public void updateCpuUtilization() {
        this.cpuUtilization++;
    }

    public void calculateDeadlineMissFrequency() {
        for (Task task : tasks) {
            if (task.getNumberOfLostDealines() > 0) {
                double deadlineMissFrequency = ((double) task.getActivations() / task.getNumberOfLostDealines());
                System.out.println("-------------------------------------------------------");
                System.out.println(task.toString() + " lost deadline at a frequency of " + deadlineMissFrequency);
            }
        }
    }

    public void checkScalability(String schedulerName) {
        double u = 0;
        for (int i = 0; i < tasksNumber; i++) {
            u += (double) (tasks.get(i).getComputationTime()) / tasks.get(i).getPeriodTime();
        }

        if (schedulerName.equals("rm")) {

            double feasible = tasksNumber * (Math.pow(2, 1.f / tasksNumber) - 1);

            if (u > feasible) {
                System.out.println("Warning, this set is not scalable to rm");
                System.out.println(u + " > " + feasible);
            } else {
                System.out.println("This set is scalable to rm");
                System.out.println(u + " <= " + feasible);
            }
        }

        if (schedulerName.equals("edf")) {

            if (u > 1) {
                System.out.println("Warning, this set is not scalable to edf");
                System.out.println(u + " > 1");
            } else {
                System.out.println("This set is scalable to edf");
                System.out.println(u + " <= 1");
            }
        }
    }

    @Override
    public String toString() {
        return "{" +
                "\nSimulation Time: " + simulationTime +
                "\nScheduler Name: '" + schedulerName + '\'' +
                "\nTasks Number: " + tasksNumber +
                "\nTasks: " + tasks +
                "\n}";
    }

}
