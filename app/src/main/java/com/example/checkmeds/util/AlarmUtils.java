package com.example.checkmeds.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.checkmeds.ui.alarma.AlarmaReceiver;

import java.util.Calendar;

public class AlarmUtils {


     //Programa múltiples alarmas para un medicamento, según su frecuencia y duración.

    public static void programarAlarmasPorFrecuencia(

            //programa multiples alarmas para un medicamento, sgun su frecuencia y duracion
            Context context,
            int idMedicamento,
            long fechaInicio,
            int frecuenciaHoras,
            int diasDuracion
    ) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Calcula el totla de las alarmas necesarioas
        int totalAlarmas = (diasDuracion * 24) / frecuenciaHoras;

        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(fechaInicio);

        // Validar que la arna sea en el futuro
        for (int i = 0; i < totalAlarmas; i++) {
            long triggerAtMillis = calendario.getTimeInMillis();

            //  Validar que el trigger sea en el futuro
            if (triggerAtMillis > System.currentTimeMillis()) {
                Intent intent = new Intent(context, AlarmaReceiver.class);
                intent.putExtra("id_medicamento", idMedicamento);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        idMedicamento * 100 + i,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }

            // Sumar la frecuencia para la siguiente alarma
            calendario.add(Calendar.HOUR_OF_DAY, frecuenciaHoras);
        }
    }

    //Cancela las alarmas programadas para un medicamento específico.
     //Usa un rango de IDs generados con idMedicamento * 100 + i.

    public static void cancelarAlarmasPorMedicamento(Context context, int idMedicamento) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancelar hasta 100 alarmas asociadas a este medicamento
        for (int i = 0; i < 100; i++) {
            Intent intent = new Intent(context, AlarmaReceiver.class);
            intent.putExtra("id_medicamento", idMedicamento);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    idMedicamento * 100 + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
        }
    }
}
