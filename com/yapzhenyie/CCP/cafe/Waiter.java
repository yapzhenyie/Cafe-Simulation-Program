package com.yapzhenyie.CCP.cafe;

import com.yapzhenyie.CCP.Main;
import com.yapzhenyie.CCP.utils.LoggerManager;

public class Waiter extends Thread {

    private final Cafe cafe;
    private final ServingArea servingArea;

    // Customer that currently serving.
    private Customer serving = null;

    // Determine is order placed by the customer.
    private boolean orderPlaced = false;

    public Waiter(Cafe cafe, ServingArea servingArea) {
        super("Waiter");
        this.cafe = cafe;
        this.servingArea = servingArea;
    }

    @Override
    public void run() {
        this.cafe.waiterInCafe = true;
        LoggerManager.printLine(this.getName() + " is on duty.");

        // If is opening time, keep serving the customers.
        while (!Main.getClock().isAfterClosingTime()) {
            // Notify the first customer to place order.
            Customer customerServing = this.cafe.serveFirstCustomerInQueue();
            if (customerServing == null) {
                continue;
            }

            this.setCustomerServing(customerServing);
            synchronized (this.servingArea) {
                this.servingArea.notifyAll();
            }

            /**
             * Once reach the closing time, the waiter will refuse to serve
             * the customer even though the customer is get notified to
             * place order.
             */
            if (Main.getClock().isAfterClosingTime()) {
                LoggerManager.printLine(this.getName() + " refuse to serve " + this.getCustomerServing().getName() +
                        " because the cafe is closed.");
                break;
            }

            // Waiting for the customer to place order.
            while (!this.isOrderPlaced()) {
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.cafe.setWaiterServingTime(System.currentTimeMillis());

            /**
             * Starts to make Cappuccino.
             */
            this.servingArea.obtainCup(this);

            boolean coffee = false;
            boolean milk = false;
            while (true) {
                if (!coffee) {
                    coffee = this.servingArea.obtainCoffee(this);
                }
                if (!milk) {
                    milk = this.servingArea.obtainMilk(this);
                }
                if (coffee && milk) {
                    break;
                } else if (coffee && !milk) {
                    /**
                     * If both resources are obtained by different people(owner & waiter),
                     * and if owner's serving time is earlier than the waiter's serving time,
                     * then return the resource back to cupboard.
                     */
                    if (this.servingArea.isResourcesTookByDifferentPeople() &&
                            this.cafe.getWaiterServingTime() >= this.cafe.getOwnerServingTime()) {
                        this.servingArea.returnCoffee(this);
                        coffee = false;
                        LoggerManager.printLine(this.getName() + " returned coffee.");
                    }
                } else if (!coffee && milk) {
                    /**
                     * If both resources are obtained by different people(owner & waiter),
                     * and if owner's serving time is earlier than the waiter's serving time,
                     * then return the resource back to cupboard.
                     */
                    if (this.servingArea.isResourcesTookByDifferentPeople() &&
                            this.cafe.getWaiterServingTime() >= this.cafe.getOwnerServingTime()) {
                        this.servingArea.returnMilk(this);
                        milk = false;
                        LoggerManager.printLine(this.getName() + " returned milk.");
                    }
                }
                try {
                    Thread.sleep(1L); // 4 seconds
                } catch (InterruptedException e) {
                }
            }
            this.servingArea.makeCappuccino(this);

            // Serve the drink to the customer.
            try {
                Thread.sleep(2L); // 4 seconds
            } catch (InterruptedException e) {
            }
            LoggerManager.printLine(this.getName() + ": " + this.getCustomerServing().getName() + " is served.");
            this.getCustomerServing().setServed(true);
            this.cafe.addCustomersServedByWaiter();
            synchronized (this) {
                this.notify();
            }
            this.setCustomerServing(null);
            this.setOrderPlaced(false);
        }
        this.cafe.waiterInCafe = false;
        LoggerManager.printLine(this.getName() + " left the cafe.");
    }

    public boolean isOrderPlaced() {
        return this.orderPlaced;
    }

    public void setOrderPlaced(boolean orderPlaced) {
        this.orderPlaced = orderPlaced;
    }

    public synchronized void setCustomerServing(Customer serving) {
        this.serving = serving;
    }

    public synchronized Customer getCustomerServing() {
        return this.serving;
    }
}
