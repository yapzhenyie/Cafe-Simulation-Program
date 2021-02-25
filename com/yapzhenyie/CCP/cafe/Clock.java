package com.yapzhenyie.CCP.cafe;

/**
 * Clock design.
 * 1 millisecond = 2 seconds in simulation time
 * The clock time is 2000x faster than the real time.
 */
public class Clock extends Thread {

    private final Cafe cafe;
    private final ServingArea servingArea;
    private final Owner owner;

    private long startingTime;
    private final int openTime = 600 * 60; // 10 AM in seconds.
    private final int closeTime = openTime + 720 * 60; // 12 hours after the open time. Usually is 10 PM in seconds.

    public Clock(Cafe cafe, ServingArea servingArea, Owner owner) {
        this.cafe = cafe;
        this.servingArea = servingArea;
        this.owner = owner;
    }

    @Override
    public void run() {
        // Start counting the time.
        this.startingTime = System.currentTimeMillis();
        boolean announced = false;
        while (true) {
            // Notify the owner to call last orders message.
            if (!announced && isLastOrderTime()) {
                this.owner.callLastOrders();
                announced = true;
            }
            if (!isAfterClosingTime()) {
                continue;
            }

            // The owner call cafe closed message.
            this.owner.callCafeClosed();

            /**
             * In case any customer waiting to place order, notify all
             * customers to leave cafe.
             */
            synchronized (this.servingArea) {
                this.servingArea.notifyAll();
            }
            break;
        }
    }

    /**
     * Get simulation time.
     *
     * @return hh:mm
     */
    public String getTime() {
        long seconds = openTime + (System.currentTimeMillis() - startingTime) * 2;

        int hour = (int) seconds / 3600;
        seconds = seconds - (hour * 3600);
        int minutes = (int) seconds / 60;

        if (isAfterClosingTime()) {
            if (hour > 23) {
                return "Closed";
            }
            return hour + ":" + String.format("%02d", minutes) + " - Closed";
        }
        return hour + ":" + String.format("%02d", minutes);
    }

    /**
     * Checks whether is 10 minutes
     * before the #closeTime 10PM.
     */
    public boolean isLastOrderTime() {
        long seconds = openTime + (System.currentTimeMillis() - startingTime) * 2;
        return seconds >= (closeTime - 10 * 60);
    }

    /**
     * Checks whether the clock is reach 10PM.
     *
     * @return true if the clock is reach 10PM.
     */
    public boolean isAfterClosingTime() {
        long seconds = openTime + (System.currentTimeMillis() - startingTime) * 2;
        return seconds >= closeTime;
    }

    /**
     * Get the starting time in milliseconds.
     *
     * @return startingTime
     */
    public long getStartingTime() {
        return this.startingTime;
    }
}
