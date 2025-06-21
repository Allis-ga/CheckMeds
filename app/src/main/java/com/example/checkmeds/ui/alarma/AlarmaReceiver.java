package com.example.checkmeds.ui.alarma;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.checkmeds.R;

//Broadcast que se encarga de manejar el evento de la alarma
//Muestra una notificación, reproduce sonido de alarma y gestiona el ID del medicamento asociado.
public class AlarmaReceiver extends BroadcastReceiver {

    //ID del canal de notificaciones
    private static final String CHANNEL_ID = "checkmeds_channel_id";
    // ID de la notificación
    private static final int NOTIFICATION_ID = 1;
    // Reproductor de sonido para la alarma
    private static MediaPlayer mediaPlayer;
    //ID del medicamento que está sonando actualmente
    private static int idMedicamentoSonando = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        int idMedicamento = intent.getIntExtra("id_medicamento", -1);

        if (idMedicamento != -1) {
            crearCanalDeNotificacion(context); // crea el canal de notificaciones si se requiere
            mostrarNotificacion(context, idMedicamento); // Muestra la notificacion de la alarma
            reproducirSonido(context); // reproduce el sonido de la alarma
            setMedicamentoSonando(idMedicamento); // 🔵 Guardar ID del medicamento sonando
        } else {
            Log.e("AlarmaReceiver", "ID de medicamento no encontrado en el Intent.");
        }
    }

    // Crea canal de notificaciones
    private void crearCanalDeNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarmas de Medicamentos";
            String description = "Notificaciones para recordar medicamentos en CheckMeds";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Muestra la notificacion interactiva con acciones para tomar, posponer
    private void mostrarNotificacion(Context context, int idMedicamento) {
        Intent intentAlarma = new Intent(context, AlarmaActivity.class);
        intentAlarma.putExtra("id_medicamento", idMedicamento);
        intentAlarma.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntentAlarma = PendingIntent.getActivity(
                context,
                0,
                intentAlarma,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para posponer la alarma
        Intent intentPosponer = new Intent(context, PosponerReceiver.class);
        intentPosponer.putExtra("id_medicamento", idMedicamento);

        PendingIntent pendingIntentPosponer = PendingIntent.getBroadcast(
                context,
                1,
                intentPosponer,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // se contruye la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("¡Hora de tomar tu medicamento!")
                .setContentText("Toca para registrar la toma o posponer.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntentAlarma)
                .addAction(R.drawable.ic_check_circle, "Tomar Medicamento", pendingIntentAlarma)
                .addAction(R.drawable.ic_snooze, "Posponer", pendingIntentPosponer);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Mostrar la notificación dependiendo de los permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    //Reproduce el sonido de la alarma en bucle.
    private void reproducirSonido(Context context) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.butterflies);
            }

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e("AlarmaReceiver", "Error reproduciendo sonido: " + e.getMessage());
        }
    }

    //Detiene y libera el reproductor de sonido.
    public static void detenerSonido() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        idMedicamentoSonando = -1; // Reiniciar ID cuando se detiene sonido
    }

    //Verifica si actualmente está sonando la alarma.
    public static boolean estaSonando() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    //Establece el ID del medicamento actualmente sonando.
    public static void setMedicamentoSonando(int id) {
        idMedicamentoSonando = id;
    }

    //Obtiene el ID del medicamento actualmente sonando.
    public static int getMedicamentoSonandoId() {
        return idMedicamentoSonando;
    }
}
