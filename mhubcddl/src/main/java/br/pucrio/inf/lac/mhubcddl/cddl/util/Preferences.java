package br.pucrio.inf.lac.mhubcddl.cddl.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lcmuniz on 15/02/17.
 */

public class Preferences {

    public static String CLIENT_ID_KEY = "CLIENTE_ID_KEY";
    public static String PREFERENCE_FILE_KEY = "PREFERENCE_FILE_KEY";

    private final Context ctx;
    private final String name;

    public Preferences(Context ctx, String name) {
        this.ctx = ctx;
        this.name = name;
    }

    public void put(String key, String value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");

    }

}
