/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.cddl.util;

import java.util.Date;

/**
 * @author bertodetacio
 */
public class Time {

    private static Time instance = null;

    /**
     *
     */
    private Time() {
        // TODO Auto-generated constructor stub
    }

    public static Time getInstance() {
        if (instance == null) {
            instance = new Time();
        }
        return instance;
    }


    public long getCurrentTimestamp() {
        Date date = new Date();
        return date.getTime();
    }

    public Date getData() {
        Date date = new Date();
        return date;
    }


}
