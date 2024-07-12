package com.example.computer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.example.resources.*;

public class Scheduler {
    // To hold all simulation data
    private Simulation simulation;
    // To control processing
    private Task[] cpu;
    // Clone of tasks for manipulation
    private List<Task> tasks;
    // Queue of tasks to be processed
    private List<Task> readyQueue;
    // For visualization simplicity
    private String schedulerName;

    // Constructor to initialize the scheduler with a simulation
    public Scheduler(Simulation simulation) {
        try {
            this.simulation = simulation;
            this.cpu = new Task[1];
            this.tasks = simulation.getTasks();
            this.readyQueue = new ArrayList<Task>();
            this.schedulerName = simulation.getSchedulerName();

        } catch (NullPointerException e) {
            System.out.println("Simulation is not set");
        }

    }

    // Method to start the scheduling simulation
    public void start() {
        if (simulation == null) {
            System.out.println("This simulation is null and cannot run");
            return;
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("Scheduling tasks accordingly to " + schedulerName);
        // Check scalability
        if (schedulerName.equals("rm") || schedulerName.equals("edf")) {
            simulation.checkScalability(schedulerName);
        }
        System.out.println("-------------------------------------------------------");

        for (int i = 0; i < simulation.getSimulationTime(); i++) {
            addToReadyQueue(i);
            organizeReadyQueue(schedulerName);
            addToCpu();
            if (cpu[0] != null) {
                compute(i);
                simulation.updateCpuUtilization();
            }
            currentStatus(i);
        }
        completeLog();

    }

    // Method to add tasks to the readyQueue based on their offset and period
    public void addToReadyQueue(int time) {
        for (Task task : tasks) {
            // Tasks enter either by their offset or by a multiple of their period
            if (task.getOffset() == time || (time - task.getOffset()) % task.getPeriodTime() == 0) {
                readyQueue.add(task);
                if (task.getComputationTimeLeft() == task.getComputationTime()) {
                    task.setActivations(task.getActivations() + 1);
                    task.setRelativeDeadline(time);
                    task.getWatList().add(0.0);
                    task.getTatList().add(0.0);
                }
            }
            // This method goes here to respect the fcfs principle and avoid inversion
            if (task == cpu[0]) {
                removeFromCpu(schedulerName);
            }
        }
    }

    // Method to organize the readyQueue based on the scheduling method
    public void organizeReadyQueue(String schedulerName) {
        switch (schedulerName) {
            case "rm":
                // Sort tasks based on period time
                Collections.sort(readyQueue, Comparator.comparingInt(Task::getPeriodTime));
                break;
            case "edf":
                // Sort tasks based on relative deadline
                Collections.sort(readyQueue, Comparator.comparingInt(Task::getRelativeDeadline));
                break;
            default:
                break;
        }
    }

    // Method to add a task to the CPU for processing
    // Checks the scheduling method
    public void addToCpu() {
        if (!readyQueue.isEmpty()) {
            if (cpu[0] == null || shouldPreempt()) {
                if (cpu[0] != null) {
                    readyQueue.add(cpu[0]);
                }
                cpu[0] = readyQueue.remove(0);
            }
        }

    }

    // Method to determine if preemption is needed based on scheduling method
    private boolean shouldPreempt() {
        return (schedulerName.equals("rm") && readyQueue.get(0).getPeriodTime() < cpu[0].getPeriodTime()) ||
                (schedulerName.equals("edf") && readyQueue.get(0).getRelativeDeadline() < cpu[0].getRelativeDeadline());
    }

    // Method to compute the task in the CPU
    public void compute(int time) {
        // Compute an instant
        cpu[0].setComputationTimeLeft(cpu[0].getComputationTimeLeft() - 1);
        // Compute a quantum
        cpu[0].setQuantumLeft(cpu[0].getQuantumLeft() - 1);
        // Task in CPU ages by 1 instant
        cpu[0].getTatList().set(cpu[0].getWatList().size() - 1,
                (cpu[0].getTatList().get(cpu[0].getTatList().size() - 1) + 1));
        // Update relative deadline of the processing task
        cpu[0].setRelativeDeadline(time);
        // Task in cpu processed, for checking starvation
        cpu[0].setProcessed();
        // Advance waiting and turnaround times of other tasks, and their relative
        // deadlines
        for (Task task : readyQueue) {
            // Task must not be in the cpu to age
            if (task != cpu[0]) {
                task.getWatList().set(task.getWatList().size() - 1,
                        (task.getWatList().get(task.getWatList().size() - 1)) + 1);
                task.getTatList().set(task.getTatList().size() - 1,
                        (task.getTatList().get(task.getTatList().size() - 1) + 1));
                task.setRelativeDeadline(time);
            }
        }
    }

    // Method to remove a task from the CPU due to quantum expiry or completion
    public void removeFromCpu(String schedulerName) {
        if (cpu[0] != null) {

            if (cpu[0].getComputationTimeLeft() == 0 || (schedulerName.equals("rr") && cpu[0].getQuantumLeft() == 0)) {
                if (cpu[0].getComputationTimeLeft() == 0) {
                    cpu[0].setQuantumLeft(cpu[0].getQuantum());
                    cpu[0].setComputationTimeLeft(cpu[0].getComputationTime());
                } else {
                    cpu[0].setQuantumLeft(cpu[0].getQuantum());
                    // Problem here
                    readyQueue.add(cpu[0]);
                }
                cpu[0] = null;
            }
        }

    }

    // Method to check for deadline misses
    public void checkLostDealine(int time) {
        for (Task task : tasks) {
            if (time == task.getActivations() * task.getDeadline() && task.getComputationTimeLeft() != 0) {
                task.setNumberOfLostDealines(task.getNumberOfLostDealines() + 1);
                System.out.println("-------------------------------------------------------");
                System.out.println(task.toString() + " lost dealine at " + time);
            }
        }
    }

    // Method to display current status at each time interval
    public void currentStatus(int time) {
        System.out.println("# CURRENT TIME: " + time + " -> " + (time + 1));

        if (cpu[0] != null) {
            System.out.println("# PROCESSING: " + cpu[0]);
            if (cpu[0].getComputationTimeLeft() == 0) {
                System.out.println("# " + cpu[0] + " completed");
            }
        } else {
            System.out.println("# PROCESSING: - ");
        }

        System.out.println("# READY QUEUE: " + readyQueue);

        if (schedulerName.equals("rm") || schedulerName.equals("edf")) {
            checkLostDealine(time);
        }

        System.out.println("-------------------------------------------------------");
    }

    // Method to log final calculations
    public void completeLog() {
        double systemTotalWat = 0;
        double systemTotalTat = 0;

        for (Task task : tasks) {
            systemTotalWat += task.getAvgWat();
            systemTotalTat += task.getAvgTat();
        }

        double avgCpuUse = Math.round(simulation.getCpuUtilization() / simulation.getSimulationTime() * 10000) / 100.0;
        double systemAvgWat = Math.round((systemTotalWat / simulation.getTasksNumber()) * 100.0) / 100.0;
        double systemAvgTat = Math.round(systemTotalTat / simulation.getTasksNumber() * 100.0) / 100.0;

        // Display relevant system-wide data
        System.out.println("CPU Utilization: " + (avgCpuUse) + "%");
        System.out.println("System Average Turnaround Time: " + systemAvgTat);
        System.out.println("System Average Waiting Time: " + systemAvgWat);

        // Check for starvation
        for (Task task : tasks) {
            if (task.isProcessed() == false) {
                System.out.println(task.toString() + " was starved");
            }
        }

        // Check deadline miss frequency for real-time schedulers
        if (schedulerName.equals("rm") || schedulerName.equals("edf")) {
            simulation.calculateDeadlineMissFrequency();
        }

        // Display data for each task
        System.out.println("-------------------------------------------------------");
        for (Task task : tasks) {
            System.out.println(task.data());
        }
        System.out.println("-------------------------------------------------------");
    }
}