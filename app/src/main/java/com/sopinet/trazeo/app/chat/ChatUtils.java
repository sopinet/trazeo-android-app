package com.sopinet.trazeo.app.chat;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatUtils {

    /**
     * Funcion para obtener la fecha formateada
     * @return Time in string type
     */
    public static String getTime() {
        return String.valueOf(System.currentTimeMillis());
    }



    /**
     * Aplica formato a la fecha
     * @param time time
     * @return Time in string formatted
     */
    public static String formatTime(String time) {
        Long timeLong;
        try {
            timeLong = Long.valueOf(time);
            Date date = new Date(timeLong);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.d("ERROR FECHA", "Error en la conversión de la fecha. " + time);
        }
        return "";
    }

    public static String formatDateNormal(String time) {
        long timeLong;
        String newDate = "";
        try {
            timeLong = Long.parseLong(time);
            Date date = new Date(timeLong);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            newDate = sdf.format(date);
        } catch (Exception e) {
            Log.d("TEMA", "Error en la conversión de la fecha.");
        }
        return newDate;
    }

    public static String formatDateNormalWithoutTime(String time) {
        long timeLong;
        String newDate = "";
        try {
            timeLong = Long.parseLong(time);
            Date date = new Date(timeLong);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            newDate = sdf.format(date);
        } catch (Exception e) {
            Log.d("TEMA", "Error en la conversión de la fecha.");
        }
        return newDate;
    }

}
