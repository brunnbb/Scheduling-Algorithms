package com.example.computer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.example.resources.*;

public class Scheduler {
    private Simulation simulation;
    private Task[] cpu;
    private List<Task> tasks;
    private List<Task> readyQueue;
    private String schedulerName;

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

    public void start() {
        if (simulation == null) {
            System.out.println("This simulation is null and cannot run");
            return;
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("Scheduling tasks accordingly to " + schedulerName);
        if (schedulerName.equals("rm") || schedulerName.equals("edf")) {
            simulation.checkScalability(schedulerName);
        }
        System.out.println("-------------------------------------------------------");

        for (int i = 0; i < simulation.getSimulationTime(); i++) {
            addToReadyQueue(i);
            organizeReadyQueue();
            addToCpu();
            organizeReadyQueue();
            if (cpu[0] != null) {
                compute();
                simulation.updateCpuUtilization();
            }
            currentStatus(i);
            removeFromCpu();
        }
        completeLog();

    }

    public void addToReadyQueue(int time) {
        for (Task task : tasks) {
            if (task.getOffset() == time
                    || ((time - task.getOffset()) % task.getPeriodTime() == 0) && task.isProcessed()) {
                readyQueue.add(task);
                if (task.getComputationTimeLeft() == task.getComputationTime()) {

                    task.setActivations(task.getActivations() + 1);
                    task.getWatList().add(0.0);
                    task.getTatList().add(0.0);
                    task.setAbsoluteDeadline();
                }
            }
        }
    }

    public void organizeReadyQueue() {
        switch (schedulerName) {
            case "rm":
                Collections.sort(readyQueue, Comparator.comparingInt(Task::getPeriodTime));
                break;
            case "edf":
                Collections.sort(readyQueue, Comparator.comparingInt(Task::getAbsoluteDeadline)
                        .thenComparing(Comparator.comparingInt(Task::getDeadline).reversed()));
                break;
            default:
                break;
        }
    }

    public void addToCpu() {
        if (!readyQueue.isEmpty()) {
            if (cpu[0] == null || shouldPreempt()) {
                if (cpu[0] != null) {
                    cpu[0].setQuantumLeft(cpu[0].getQuantum());
                    readyQueue.add(cpu[0]);
                }
                cpu[0] = readyQueue.remove(0);
            }
        }

    }

    private boolean shouldPreempt() {
        return (schedulerName.equals("rm") && readyQueue.get(0).getPeriodTime() < cpu[0].getPeriodTime()) ||
                (schedulerName.equals("edf") && readyQueue.get(0).getAbsoluteDeadline() < cpu[0].getAbsoluteDeadline())
                || (schedulerName.equals("rr") && cpu[0].getQuantumLeft() == 0);
    }

    public void compute() {
        int lastTatItemCpu = cpu[0].getTatList().size() - 1;
        cpu[0].setComputationTimeLeft(cpu[0].getComputationTimeLeft() - 1);
        cpu[0].setQuantumLeft(cpu[0].getQuantumLeft() - 1);
        cpu[0].getTatList().set(lastTatItemCpu, (cpu[0].getTatList().get(lastTatItemCpu) + 1));
        cpu[0].setProcessed();
        for (Task task : readyQueue) {
            if (task != cpu[0]) {
                int lastWatItemTask = task.getTatList().size() - 1;
                int lastTatItemTask = task.getWatList().size() - 1;
                task.getWatList().set(lastWatItemTask, (task.getWatList().get(lastWatItemTask)) + 1);
                task.getTatList().set(lastTatItemTask, (task.getTatList().get(lastTatItemTask) + 1));
            }
        }
    }

    public void removeFromCpu() {
        if (cpu[0] != null && cpu[0].getComputationTimeLeft() == 0) {
            cpu[0].setQuantumLeft(cpu[0].getQuantum());
            cpu[0].setComputationTimeLeft(cpu[0].getComputationTime());
            cpu[0] = null;
        }

    }

    public void checkLostDealine(int time) {
        for (Task task : tasks) {
            if (time == task.getAbsoluteDeadline() && task.getComputationTimeLeft() != 0) {
                task.setNumberOfLostDealines(task.getNumberOfLostDealines() + 1);
                System.out.println("-------------------------------------------------------");
                System.out.println(task.toString() + " lost dealine at " + time);
            }
        }
    }

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

        System.out.println("CPU Utilization: " + (avgCpuUse) + "%");
        System.out.println("System Average Turnaround Time: " + systemAvgTat);
        System.out.println("System Average Waiting Time: " + systemAvgWat);

        for (Task task : tasks) {
            if (task.isProcessed() == false) {
                System.out.println(task.toString() + " was starved");
            }
        }

        if (schedulerName.equals("rm") || schedulerName.equals("edf")) {
            simulation.calculateDeadlineMissFrequency();
        }

        System.out.println("-------------------------------------------------------");
        for (Task task : tasks) {
            System.out.println(task.data());
        }
        System.out.println("-------------------------------------------------------");
    }
}