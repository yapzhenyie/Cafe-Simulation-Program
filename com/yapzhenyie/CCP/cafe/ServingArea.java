package com.yapzhenyie.CCP.cafe;

import com.yapzhenyie.CCP.utils.LoggerManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServingArea {

    // Determine which thread took the coffee. (Owner, Waiter or null)
    private Thread coffeeTookBy = null;

    // Determine which thread took the milk. (Owner, Waiter or null)
    private Thread milkTookBy = null;

    // Locks to prevent multiple threads access the shared resources at the same time.
    private Lock coffeeLock = new ReentrantLock();
    private Lock milkLock = new ReentrantLock();

    /**
     * Obtain cup.
     * Takes 4 seconds of the simulation time.
     *
     * @param thread The owner or the waiter.
     */
    public void obtainCup(Thread thread) {
        try {
            Thread.sleep(2L); // 4 seconds
        } catch (InterruptedException ignored) {
        }
        LoggerManager.printLine(thread.getName() + " take a cup.");
    }

    /**
     * Obtain coffee.
     * Takes 4 seconds of the simulation time.
     *
     * @param thread The owner or the waiter.
     */
    public boolean obtainCoffee(Thread thread) {
        try {
            Thread.sleep(2L); // 4 seconds
        } catch (InterruptedException ignored) {
        }
        if (!coffeeLock.tryLock()) {
            return false;
        }
        coffeeTookBy = thread;
        LoggerManager.printLine(thread.getName() + " obtained coffee.");
        return true;
    }

    /**
     * Obtain milk.
     * Takes 4 seconds of the simulation time.
     *
     * @param thread The owner or the waiter.
     */
    public boolean obtainMilk(Thread thread) {
        try {
            Thread.sleep(2L); // 4 seconds
        } catch (InterruptedException ignored) {
        }
        if (!milkLock.tryLock()) {
            return false;
        }
        milkTookBy = thread;
        LoggerManager.printLine(thread.getName() + " obtained milk.");
        return true;
    }

    /**
     * Make the cappuccino after obtained cup, coffee
     * and milk ingredients.
     * <p>
     * Takes 5 minutes of the simulation time.
     *
     * @param thread The owner or the waiter.
     */
    public void makeCappuccino(Thread thread) {
        try {
            Thread.sleep(150L); // 5 minutes
            LoggerManager.printLine(thread.getName() + " made a cappuccino.");

            returnCoffee(thread);
            returnMilk(thread);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Return coffee ingredient back to cupboard.
     * Takes 4 seconds of the simulation time.
     *
     * @param thread The owner or the waiter.
     */
    public void returnCoffee(Thread thread) {
        try {
            Thread.sleep(2L); // 4 seconds
        } catch (InterruptedException ignored) {
        }
        coffeeTookBy = null;
        coffeeLock.unlock();
    }

    /**
     * Return milk ingredient back to cupboard.
     * Takes 4 seconds of the simulation time.
     *
     * @param thread The owner or the waiter.
     */
    public void returnMilk(Thread thread) {
        try {
            Thread.sleep(2L); // 4 seconds
        } catch (InterruptedException ignored) {
        }
        milkTookBy = null;
        milkLock.unlock();
    }

    /**
     * Check whether is coffee and milk obtained by different thread.
     * (Owner and Waiter)
     */
    public boolean isResourcesTookByDifferentPeople() {
        if (coffeeTookBy == null || milkTookBy == null) {
            return false;
        }
        return coffeeTookBy != milkTookBy;
    }
}
