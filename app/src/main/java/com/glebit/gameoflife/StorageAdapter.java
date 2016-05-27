package com.glebit.gameoflife;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Itenberg on 27.05.2016.
 */
public final class StorageAdapter
{
    // сериализуем массив в json
    // сохраняем строку json в shared preferences
    public static void savePreset(byte[][] preset, String name, Context context)
    {
        String serializedArray=serializeArray(preset);
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(name, serializedArray).commit();
    }

    // получаем список всех ключей в shared preferences
    public static ArrayList<String> getConfigs(Context context)
    {
        ArrayList<String> lstKeys=new ArrayList<>();

        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, ?> keys= pref.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet())
            lstKeys.add(entry.getKey());

        return lstKeys;
    }

    // загружаем сохраненный конфиг
    public static byte[][] loadPreset(String name, Context context)
    {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        String json=pref.getString(name, "");
        return deserialize(json);
    }

    // десериализация строки в массив byte[][]
    private static byte[][] deserialize(String jsonString)
    {
        Gson gson=new Gson();
        byte[][] array=gson.fromJson(jsonString, byte[][].class);
        return  array;
    }

    // сериализация в json
    private static String serializeArray(byte[][] array)
    {
        Gson gson=new Gson();
        return gson.toJson(array);
    }
}
