package com.yapzhenyie.CCP.utils;

import com.yapzhenyie.CCP.Main;

/**
 * LoggerManager
 * <p>
 * Some utilities where print output message in a structure way.
 */
public class LoggerManager {

    /**
     * print output message with time prefix.
     *
     * @param msg The output message.
     */
    public static void printLine(String msg) {
        System.out.println("[" + Main.getClock().getTime() + "] " + msg);
    }
}
