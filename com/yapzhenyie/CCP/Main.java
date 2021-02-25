package com.yapzhenyie.CCP;

import com.yapzhenyie.CCP.cafe.*;
import com.yapzhenyie.CCP.utils.CafeScenario;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static Clock clock;

    // Scenario enum.
    private static CafeScenario scenario = CafeScenario.NORMAL_LOAD;

    /**
     * Main method.
     * Prompt input from the user to select a scenario to simulate.
     */
    public static void main(String[] args) {
        System.out.println("Cafe Scenarios - Choose a scenario from the list below to simulate the scenario.");
        System.out.println("\t1. Run the program with normal customers load.");
        System.out.println("\t2. Run the program with a busy day simulation.");
        System.out.println("\t3. Run the program without customers entering the cafe.");
        System.out.println("\t4. Run the program without the waiter working for the day.");
        System.out.print("Select an option(1-4): ");
        Scanner scanner = new Scanner(System.in);
        int input = 0;
        try {
            input = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Incorrect format. Please type in number format.");
        }

        while (input <= 0 || input > 4) {
            System.out.print("Select an option(1-4): ");
            scanner = new Scanner(System.in);
            try {
                input = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Incorrect format. Please type in number format.");
            }
        }
        switch (input) {
            case 1:
            default:
                scenario = CafeScenario.NORMAL_LOAD;
                break;
            case 2:
                scenario = CafeScenario.BUSY_DAY;
                break;
            case 3:
                scenario = CafeScenario.NO_CUSTOMER;
                break;
            case 4:
                scenario = CafeScenario.NO_WAITER;
                break;
        }

        System.out.println("");
        System.out.println("------------Selected Scenario: " + scenario.getDescription() + "------------");
        startSimulation();
    }

    /**
     * Starts the simulation based on the selected scenario.
     */
    public static void startSimulation() {
        Cafe cafe = new Cafe();
        ServingArea servingArea = new ServingArea();
        Owner owner = new Owner(cafe, servingArea);
        Waiter waiter = null;

        // Scenario: without the waiter working for the day.
        if (scenario != CafeScenario.NO_WAITER) {
            waiter = new Waiter(cafe, servingArea);
        }

        clock = new Clock(cafe, servingArea, owner);
        clock.start();

        owner.start();
        if (waiter != null)
            waiter.start();

        Customer[] customers = null;
        switch (scenario) {
            case NO_CUSTOMER:
                break;
            case BUSY_DAY:
                customers = new Customer[150];
                break;
            case NORMAL_LOAD:
            default:
                customers = new Customer[100];
                break;
        }

        if (customers != null) {
            for (int i = 0; i < customers.length; i++) {
                customers[i] = new Customer("Customer " + (i + 1), cafe, servingArea, owner, waiter);
            }

            for (int i = 0; i < customers.length; i++) {
                customers[i].start();
            }
        }

        try {
            if (customers != null) {
                for (int i = 0; i < customers.length; i++) {
                    customers[i].join();
                }
            }
            if (waiter != null)
                waiter.join();
            owner.join();
        } catch (InterruptedException e) {
        }

        System.out.println("");
        System.out.println("Scenario: " + scenario.getDescription());
        System.out.println("------------Statistics of the Simulation------------");
        System.out.println("Number of Customers Came In: " + cafe.getCustomersCameIn());
        System.out.println("Number of Customers Served: " + cafe.getCustomersServed() + " - (Owner: " +
                cafe.getCustomersServedByOwner() + ", Waiter: " + cafe.getCustomersServedByWaiter() + ")");
        System.out.println("Number of Customers Left Without Buying: " + (cafe.getCustomersCameIn() - cafe.getCustomersServed()));
        System.out.println("Average Waiting Time: " + cafe.getAverageWaitingTime());
        System.out.println("Maximum Waiting Time: " + cafe.getMaximumWaitingTime());
        System.out.println("Minimum Waiting Time: " + cafe.getMinimumWaitingTime());
        System.out.println("");
        System.out.println("Time Consumed(Real-time): " + String.format("%.2f", (float) (System.currentTimeMillis() - getClock().getStartingTime()) / 1000) + "s");
        //System.out.println(cafe.customerWaitingTime);
        //System.out.println("Customers to Serve: " + cafe.customersToServe);
    }

    /**
     * Clock instance
     * Used to check whether is closing time.
     */
    public static Clock getClock() {
        return clock;
    }
}
