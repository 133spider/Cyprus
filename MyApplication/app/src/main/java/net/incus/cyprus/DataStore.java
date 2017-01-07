package net.incus.cyprus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by spider on 9/26/2016.
 */

public class DataStore extends Application {

    private static HashMap<String, Object> keys;
    private static SharedPreferences sharedpreferences;
    private static SharedPreferences.Editor editor;

    /** initialize preset variables **/
    public static void init(){
        keys = new HashMap<String, Object>();
    }

    public static Object get(Object key){
        if(keys == null)
            init();
        return keys.get(key);
    }

    public static void append(Context context, String key, Object value, Boolean save){
        if(keys == null)
            init();
        keys.put(key, value);
        if(save) {
            sharedpreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            editor = sharedpreferences.edit();
            editor.putString(key, (String)value);
            editor.commit();
            return;
        }
    }

    public static void remove(String key){
        if(keys == null)
            init();
    }

    public static boolean exists(String key){
        if(keys == null)
            init();
        if(keys.get(key) != null)
            return true;
        return false;
    }

}
