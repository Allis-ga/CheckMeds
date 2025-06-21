package com.example.checkmeds.ui.alarma;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.example.checkmeds.R;
import com.example.checkmeds.data.CheckMedsDbHelper;
import com.example.checkmeds.data.Medicamento;
import com.example.checkmeds.ui.configuracion.ConfiguracionActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

//Se muestra cuando suena una alarma para tomar el medicamento
public class AlarmaActivity extends AppCompatActivity {

    //Vsitas de la interfaz
    private TextView tvAlerta, tvNombre, tvDosis;
    private ImageView imgMedicamento;
    private Button btnTomar, btnPosponer, btnOmitir;

    //Datos del medicamento y la hora
    private int idMedicamento;
    private Medicamento medicamento;
    private Uri uriImagenTomada;

    private static final int REQUEST_TOMAR_FOTO = 101;
    private CheckMedsDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        //Cancelar notificación visible sin detener el sonido
        NotificationManagerCompat.from(this).cancel(1);

        //inicializa la base de datos
        dbHelper = new CheckMedsDbHelper(this);

        //inicializa los componentes de la interfaz
        tvAlerta = findViewById(R.id.tvAlerta);
        tvNombre = findViewById(R.id.tvNombreMedicamento);
        tvDosis = findViewById(R.id.tvDosisMedicamento);
        imgMedicamento = findViewById(R.id.imgMedicamento);
        btnTomar = findViewById(R.id.btnTomar);
        btnPosponer = findViewById(R.id.btnPosponer);
        btnOmitir = findViewById(R.id.btnOmitir);

        //Obtiene el ID del medicamento desde el Intent que llamo a esta activiadad
        idMedicamento = getIntent().getIntExtra("id_medicamento", -1);

        if (idMedicamento != -1) {
            // Si se recibió ID válido, obtener datos del medicamento
            medicamento = dbHelper.obtenerMedicamentoPorId(idMedicamento);
            mostrarDatosMedicamento();
        } else {
            // Si no se recibió ID, limpiar alarma y finalizar actividad
            getSharedPreferences("checkmeds_prefs", MODE_PRIVATE)
                    .edit()
                    .remove("alarma_sonando")
                    .remove("alarma_id_medicamento")
                    .apply();
            Toast.makeText(this, "Error: ID de medicamento no recibido", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Botón para registrar toma del medicamento
        btnTomar.setOnClickListener(v -> abrirCamara());

        // Botón para omitir la alarma
        btnOmitir.setOnClickListener(v -> {
            detenerSonido();
            registrarEvento("Omitido", null, true);
            limpiarEstadoAlarma();
            Toast.makeText(this, "Toma omitida", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Botón para posponer la alarma
        btnPosponer.setOnClickListener(v -> {
            detenerSonido();
            registrarEvento("Pospuesto", null, true);
            limpiarEstadoAlarma();
            reprogramarAlarmaSegunConfig();
            Toast.makeText(this, "Alarma pospuesta", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    //muestra los datos del medicamento en la interfaz
    private void mostrarDatosMedicamento() {
        if (medicamento != null) {
            tvNombre.setText(medicamento.getNombre());
            tvDosis.setText("Dosis: " + medicamento.getDosis());
            tvAlerta.setText("¡Hora de tomar el medicamento!");

            if (medicamento.getImagenUri() != null && !medicamento.getImagenUri().isEmpty()) {
                imgMedicamento.setImageURI(Uri.parse(medicamento.getImagenUri()));
            } else {
                imgMedicamento.setImageResource(R.drawable.ic_medication);
            }
        }
    }

    //Abre la camara para toma la fotografia de confirmacion
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Verifica si existe app de cámara
        if (intent.resolveActivity(getPackageManager()) != null) {
            File fotoArchivo = null;
            // Crear archivo temporal para la foto
            try {

                fotoArchivo = crearArchivoImagen();
            } catch (IOException ex) {
                Toast.makeText(this, "No se pudo crear archivo para la foto", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fotoArchivo != null) {// Obtener URI segura para compartir la imagen
                uriImagenTomada = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        fotoArchivo
                );
                // Indica destino de la imagen capturada
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagenTomada);
                startActivityForResult(intent, REQUEST_TOMAR_FOTO);
            }
        }
    }

    //Crea un archivo temporal para poder almacenar la foto
    private File crearArchivoImagen() throws IOException {
        // Generar nombre de archivo con fecha y hora
        String nombreArchivo = "foto_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Crear archivo temporal
        return File.createTempFile(nombreArchivo, ".jpg", directorio);
    }

    // Resultado de la actividad capturar la foto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TOMAR_FOTO && resultCode == Activity.RESULT_OK) {
            detenerSonido();
            registrarEvento("Tomado", uriImagenTomada != null ? uriImagenTomada.toString() : null, true);
            limpiarEstadoAlarma();
            Toast.makeText(this, "Toma registrada", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Registra el evento en el historial
    private void registrarEvento(String estado, String fotoUri, boolean enviarSMS) {
        dbHelper.insertarHistorial(
                idMedicamento,
                estado,
                System.currentTimeMillis(),
                fotoUri,
                enviarSMS
        );

        if (enviarSMS) {
            ConfiguracionActivity.enviarSmsEstadoDesdeFuera(this, medicamento, estado);
        }
    }


    //Reprograma una alarma despues de posponer la anterior
    private void reprogramarAlarmaSegunConfig() {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        int minutos = prefs.getInt("tiempo_posponer", 10);

        long triggerAtMillis = System.currentTimeMillis() + (minutos * 60 * 1000L);

        Intent intent = new Intent(this, AlarmaReceiver.class);
        intent.putExtra("id_medicamento", idMedicamento);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                idMedicamento + 999, // ID único para la nueva alarma
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }

    //Detiene el sonido de la alarma activa
    private void detenerSonido() {
        AlarmaReceiver.detenerSonido();
    }

    //Limpia el estado de alarma presistente en SharedPreferences
    private void limpiarEstadoAlarma() {
        getSharedPreferences("checkmeds_prefs", MODE_PRIVATE)
                .edit()
                .remove("alarma_sonando")
                .remove("alarma_id_medicamento")
                .apply();
    }
}
