package com.example.checkmeds.ui.alarma;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//BroadcastReceiver que maneja la acción de "Posponer" una alarma.
public class PosponerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Obtener el ID del medicamento desde el intent
        int idMedicamento = intent.getIntExtra("id_medicamento", -1);

        if (idMedicamento != -1) {
            // Detener el sonido que está sonando
            AlarmaReceiver.detenerSonido();

            // Reprogramar la alarma para 10 minutos después
            long posponerTiempoMillis = System.currentTimeMillis() + (10 * 60 * 1000L); // 10 minutos

            // Crear un nuevo intent que volverá a lanzar la alarma
            Intent intentAlarma = new Intent(context, AlarmaReceiver.class);
            intentAlarma.putExtra("id_medicamento", idMedicamento);

            // Crea un PendingIntent para reprogramar la alarma
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    idMedicamento, // Usar el ID del medicamento como requestCode para identificar la alarma
                    intentAlarma,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Obtener el servicio de AlarmManage
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // Programar la nueva alarma exactamente para el tiempo calculado
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        posponerTiempoMillis,
                        pendingIntent
                );
            }
        }
    }
}
