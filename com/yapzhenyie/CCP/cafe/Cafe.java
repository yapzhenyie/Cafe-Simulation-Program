package com.yapzhenyie.CCP.cafe;

import com.yapzhenyie.CCP.utils.LoggerManager;

import java.util.ArrayList;

public class Cafe {

    public static class testing {

        // Number of customers served by the owner.
        private int customersServedByOwner = 0;

        public synchronized void addCustomersServedByOwner() {
            this.customersServedByOwner++;
        }

        public synchronized int getCustomersServedByOwner() {
            return this.customersServedByOwner;
        }
    }
    // Determine whether the waiter is in the cafe.
    public boolean waiterInCafe = false;

    // Number of customers who have been in the cafe before.
    private int customersCameIn = 0;

    // Number of customers currently in cafe.
    public int customersInCafe = 0;

    // Number of customers need to serve.
    public ArrayList<Customer> customersToServe = new ArrayList<>();

    // Customers sitting on the chair. (Maximum 5)
    public ArrayList<Customer> customersSitting = new ArrayList<>();

    // Customer waiting time in ticks
    public ArrayList<Long> customerWaitingTime = new ArrayList<>();

    // Number of customers served.
    public int customersServed = 0;

    // Number of customers served by the owner.
    private int customersServedByOwner = 0;

    // Number of customers served by the waiter.
    private int customersServedByWaiter = 0;

    // The time that owner start serving a customer. (in milliseconds)
    // Used to allocate the shared resources to avoid deadlock.
    private long ownerServingTime;

    // The time that waiter start serving a customer. (in milliseconds)
    // Used to allocate the shared resources to avoid deadlock.
    private long waiterServingTime;

    /**
     * Customer came in the cafe.
     */
    public synchronized void customerCameIn(Customer customer) {
        synchronized (this) {
            this.customersCameIn++;
            this.customersInCafe++;
        }
        synchronized (this.customersToServe) {
            this.customersToServe.add(customer);
        }
        LoggerManager.printLine(customer.getName() + " came in and line up.");
    }

    /**
     * Get the first customer in queue to serve.
     *
     * @return The customer. Return null if no customers in queue.
     */
    public synchronized Customer serveFirstCustomerInQueue() {
        synchronized (this.customersToServe) {
            if (this.customersToServe.size() == 0) {
                return null;
            }
        }
        Customer cus;
        synchronized (this.customersToServe) {
            cus = this.customersToServe.remove(0);
        }
        return cus;
    }

    /**
     * Customer is looking for a seat.
     *
     * @param customer The customer.
     * @return false if all sits are occupied, true if the customer found a seat.
     */
    public boolean lookingForSeat(Customer customer) {
        synchronized (this.customersSitting) {
            if (this.customersSitting.size() >= 5) {
                return false;
            }
            this.customersSitting.add(customer);
        }
        LoggerManager.printLine(customer.getName() + " found a seat and enjoy the drink.");
        return true;
    }

    /**
     * Customer leave the seat if was sitting.
     * return the cup to the serving area and leave the cafe.
     *
     * @param customer
     * @return
     */
    public void leaveCafeAfterFinishedDrink(Customer customer) {
        synchronized (this.customersSitting) {
            if (this.customersSitting.remove(customer)) {
                LoggerManager.printLine(customer.getName() + " left the chair.");
            }
        }
        LoggerManager.printLine(customer.getName() + " return the cup to the serving area.");
        try {
            Thread.sleep(5L); // 10 seconds
        } catch (InterruptedException e) {
        }
        LoggerManager.printLine(customer.getName() + " left.");
        synchronized (this) {
            customersInCafe--;
        }
    }

    /**
     * The customer leave the cafe without buying any drink.
     * This situation happens when the cafe is closed.
     *
     * @param customer
     * @return
     */
    public void leaveCafe(Customer customer) {
        try {
            Thread.sleep(5L); // 10 seconds
        } catch (InterruptedException e) {
        }
        LoggerManager.printLine(customer.getName() + " left without buying any drink.");
        synchronized (this) {
            customersInCafe--;
        }
    }

    public synchronized int getCustomersCameIn() {
        return this.customersCameIn;
    }

    public synchronized int getCustomersServed() {
        return this.customersServed;
    }

    public synchronized void addCustomersServedByOwner() {
        this.customersServedByOwner++;
    }

    public synchronized int getCustomersServedByOwner() {
        return this.customersServedByOwner;
    }

    public synchronized void addCustomersServedByWaiter() {
        this.customersServedByWaiter++;
    }

    public synchronized int getCustomersServedByWaiter() {
        return this.customersServedByWaiter;
    }

    public synchronized void setOwnerServingTime(long timeMillis) {
        this.ownerServingTime = timeMillis;
    }

    public synchronized long getOwnerServingTime() {
        return this.ownerServingTime;
    }

    public synchronized void setWaiterServingTime(long timeMillis) {
        this.waiterServingTime = timeMillis;
    }

    public synchronized long getWaiterServingTime() {
        return this.waiterServingTime;
    }

    /**
     * Get the average waiting time per customer serve.
     */
    public String getAverageWaitingTime() {
        if (this.customerWaitingTime.size() == 0) {
            return "N/A";
        }
        long totalWaitingTicks = 0;
        for (Long waitingTime : this.customerWaitingTime) {
            totalWaitingTicks += waitingTime;
        }
        double averageWaitingTime = (double) totalWaitingTicks * 2 / this.customerWaitingTime.size() / 60;
        return String.format("%.2f", averageWaitingTime) + " minutes";
    }

    /**
     * Get the maximum waiting time among all customers served.
     */
    public String getMaximumWaitingTime() {
        if (this.customerWaitingTime.size() == 0) {
            return "N/A";
        }
        long maximumWaitingTicks = 0;
        for (Long waitingTime : this.customerWaitingTime) {
            if (waitingTime > maximumWaitingTicks) {
                maximumWaitingTicks = waitingTime;
            }
        }
        double maximumWaitingTime = (double) maximumWaitingTicks * 2 / 60;
        return String.format("%.2f", maximumWaitingTime) + " minutes";
    }

    /**
     * Get the minimum waiting time among all customers served.
     */
    public String getMinimumWaitingTime() {
        if (this.customerWaitingTime.size() == 0) {
            return "N/A";
        }
        long minimumWaitingTicks = this.customerWaitingTime.get(0);
        for (Long waitingTime : this.customerWaitingTime) {
            if (waitingTime < minimumWaitingTicks) {
                minimumWaitingTicks = waitingTime;
            }
        }
        double minimumWaitingTime = (double) minimumWaitingTicks * 2 / 60;
        return String.format("%.2f", minimumWaitingTime) + " minutes";
    }
}
