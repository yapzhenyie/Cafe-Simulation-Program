package com.yapzhenyie.CCP.cafe;

import com.yapzhenyie.CCP.Main;
import com.yapzhenyie.CCP.utils.LoggerManager;

import java.util.Random;

public class Customer extends Thread {

    private final Cafe cafe;
    private final ServingArea servingArea;
    private final Owner owner;
    private final Waiter waiter;

    // Determine whether is the customer being served.
    private boolean isServed = false;

    public Customer(String name, Cafe cafe, ServingArea servingArea, Owner owner, Waiter waiter) {
        super(name);
        this.cafe = cafe;
        this.servingArea = servingArea;
        this.owner = owner;
        this.waiter = waiter;
    }

    @Override
    public void run() {
        Random random = new Random();
        try {
            /**
             *The thread will sleep for random intervals before they
             * enter the cafe.
             * (21600 is the total milliseconds of the cafe working time)
             */
            Thread.sleep(random.nextInt(21600));
        } catch (Exception ignored) {
        }

        /*
         * Get the time when customer came in,
         * to calculate the total waiting time at the end.
         */
        long startQueueTime = System.currentTimeMillis();

        synchronized (this.servingArea) {
            // Customer came into the cafe.
            this.cafe.customerCameIn(this);

            // Customer leave the cafe if after closing time.
            if (Main.getClock().isAfterClosingTime()) {
                this.cafe.leaveCafe(this);
                return;
            }
            // Customer waiting for either waiter or owner to call to place order.
            while (true) {
                try {
                    this.servingArea.wait();
                } catch (InterruptedException ignored) {
                }

                // Get notify by Clock thread to leave the cafe as the cafe is closed.
                if (Main.getClock().isAfterClosingTime()) {
                    this.cafe.leaveCafe(this);
                    return;
                }
                /**
                 * The waiter and owner will notify all the customers who
                 * are waiting. Therefore, customer need to check whether
                 * either the Waiter or Owner is going to this customer.
                 *
                 * If found any, then will break the looping.
                 *
                 * If did not found, will continue to the next loop and wait
                 * for waiter or owner to notify.
                 */
                if (this.waiter != null) {
                    if (this.waiter.getCustomerServing() == this) {
                        break;
                    }
                }
                if (this.owner != null) {
                    if (this.owner.getCustomerServing() == this) {
                        break;
                    }
                }
            }
        }

        /**
         *  Customer leave the cafe if after closing time.
         */
        if (Main.getClock().isAfterClosingTime()) {
            this.cafe.leaveCafe(this);
            return;
        }

        boolean waitingToBeServed = true;
        if (this.waiter != null) {
            /**
             * If waiter is going to serve this customer, then the
             * customer will place order with the waiter and wait
             * for the waiter to notify when the drink is ready.
             */
            if (this.waiter.getCustomerServing() == this) {
                LoggerManager.printLine(this.getName() + " is going to order Cappuccino from Waiter.");
                this.waiter.setOrderPlaced(true);
                waitingToBeServed = false;
                synchronized (this.waiter) {
                    try {
                        this.waiter.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        if (waitingToBeServed)
            if (this.owner != null) {
                /**
                 * If owner is going to serve this customer, then the
                 * customer will place order with the owner and wait
                 * for the owner to notify when the drink is ready.
                 */
                if (this.owner.getCustomerServing() == this) {
                    LoggerManager.printLine(this.getName() + " is going to order Cappuccino from Owner.");
                    this.owner.setOrderPlaced(true);
                    waitingToBeServed = false;
                    synchronized (this.owner) {
                        try {
                            this.owner.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        /**
         * If both waiter and owner did not serve the customer,
         * this message will be displayed.
         */
        if (waitingToBeServed) {
            LoggerManager.printLine("An error occurred when serving " + this.getName());
        }

        /**
         * When the cafe is closed and the customer is not served.
         */
        if (!isServed() && Main.getClock().isAfterClosingTime()) {
            this.cafe.leaveCafe(this);
            return;
        }

        LoggerManager.printLine(this.getName() + " obtained the drink.");
        long waitingTime = System.currentTimeMillis() - startQueueTime;
        // Update customers served and waiting time data.
        synchronized (this.cafe) {
            this.cafe.customersServed++;
            synchronized (this.cafe.customerWaitingTime) {
                this.cafe.customerWaitingTime.add(waitingTime);
            }
        }
        if (this.cafe.lookingForSeat(this)) {
            /**
             * Customer found a seat and sitting enjoy the drink for 5 - 30 minutes.
             */
            try {
                // 5 - 30 minutes
                Thread.sleep((long) (new Random().nextInt(5) + 1) * 150L);
            } catch (Exception ignored) {
            }
        } else {
            /**
             * Customer did not found any seats and keep standing
             * to enjoy the drink for 5 - 15 minutes.
             */
            try {
                // 5 - 15 minutes
                Thread.sleep((long) (new Random().nextInt(2) + 1) * 150L);
            } catch (Exception ignored) {
            }
        }
        // Customer leave the cafe after finished the drink.
        this.cafe.leaveCafeAfterFinishedDrink(this);
    }

    public boolean isServed() {
        return this.isServed;
    }

    public void setServed(boolean served) {
        this.isServed = served;
    }
}
