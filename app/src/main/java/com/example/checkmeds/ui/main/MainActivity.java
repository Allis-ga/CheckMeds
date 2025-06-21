package com.example.checkmeds.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.checkmeds.R;
import com.example.checkmeds.data.CheckMedsDbHelper;
import com.example.checkmeds.data.Medicamento;
import com.example.checkmeds.ui.alarma.AlarmaActivity;
import com.example.checkmeds.ui.alarma.AlarmaReceiver;
import com.example.checkmeds.ui.configuracion.ConfiguracionActivity;
import com.example.checkmeds.ui.historial.HistorialActivity;
import com.example.checkmeds.ui.registro.DetalleMedicamentoActivity;
import com.example.checkmeds.ui.registro.RegistroMedicamentoActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

//Actividad principal
//permite visualizacion de medicamentos registrados, acceder al historial
//configuracion o registrar nuevos medicamentos
public class MainActivity extends AppCompatActivity {

    // Código para solicitar permisos
    private static final int REQUEST_PERMISOS = 123;
    // Vistas
    private LinearLayout contenedorMedicamentos;

    private Button btnNuevo, btnHistorial, btnConfig;
    // Acceso a la base de datos
    private CheckMedsDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Solicita permisos necesarios
        solicitarPermisos();

        // Asocia vistas de la interfaz
        contenedorMedicamentos = findViewById(R.id.contenedorMedicamentos);
        btnNuevo = findViewById(R.id.btnNuevo);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnConfig = findViewById(R.id.btnConfig);

        dbHelper = new CheckMedsDbHelper(this);

        // Botón Registrar nuevo medicamento
        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroMedicamentoActivity.class);
            startActivity(intent);
        });

        // Botón: Ir al historial
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            startActivity(intent);
        });

        // Botón: Ir a configuración
        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        });
    }

    //Metodo llamado para reanudar la actividad
    @Override
    protected void onResume() {
        super.onResume();

        //  Si la alarma sigue sonando, abrir la pantalla
        if (AlarmaReceiver.estaSonando()) {
            int idMed = AlarmaReceiver.getMedicamentoSonandoId();
            if (idMed != -1) {
                Intent intent = new Intent(this, AlarmaActivity.class);
                intent.putExtra("id_medicamento", idMed);
                startActivity(intent);
                return;
            }
        }

        // Actualizar medicamentos en pantalla
        mostrarMedicamentos();
    }

    //Muestra todas las tarjetas de medicamentos registrados
    private void mostrarMedicamentos() {
        contenedorMedicamentos.removeAllViews();

        List<Medicamento> lista = dbHelper.obtenerTodosLosMedicamentos();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Crear tarjeta visualmente
        for (Medicamento med : lista) {
            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(32, 24, 32, 24);
            tarjeta.setBackground(createRoundedBackground());
            tarjeta.setElevation(8f);
            tarjeta.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 32);
            tarjeta.setLayoutParams(params);

            //nombre del medicamento
            TextView tvNombre = new TextView(this);
            tvNombre.setText(med.getNombre());
            tvNombre.setTextSize(20);
            tvNombre.setTextColor(getResources().getColor(R.color.texto_titulo));
            tarjeta.addView(tvNombre);

            //proima alarma calculada
            long proximaAlarma = calcularProximaAlarma(med);
            String textoAlarma = "Próxima toma: " + formato.format(proximaAlarma);

            TextView tvProxima = new TextView(this);
            tvProxima.setText(textoAlarma);
            tvProxima.setTextSize(16);
            tvProxima.setTextColor(getResources().getColor(R.color.texto_subtitulo));
            tarjeta.addView(tvProxima);

            //accion de tocar la tarjeta para ver losdetalles
            tarjeta.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DetalleMedicamentoActivity.class);
                intent.putExtra("id_medicamento", med.getId());
                startActivity(intent);
            });

            //Agrega la tarjeta al contenedor principal
            contenedorMedicamentos.addView(tarjeta);
        }
    }

    //calcula la próxoma alarma activa de un medicamento
    private long calcularProximaAlarma(Medicamento med) {
        long inicio = med.getFechaInicio();
        int frecuenciaHoras = med.getFrecuenciaHoras();
        int totalAlarmas = (med.getDiasDuracion() * 24) / frecuenciaHoras;

        long ahora = System.currentTimeMillis();
        long siguiente = inicio;

        for (int i = 0; i < totalAlarmas; i++) {
            if (siguiente > ahora) {
                return siguiente;
            }
            siguiente += frecuenciaHoras * 60 * 60 * 1000L;
        }

        return ahora; // si ya terminó el tratamiento
    }

    //Crea el fondo redondeado para las tarjetas
    private GradientDrawable createRoundedBackground() {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(getResources().getColor(R.color.boton_primario_texto));
        shape.setCornerRadius(40f);
        return shape;
    }

    private void solicitarPermisos() {
        String[] permisos;
        //Ajustar los permisos necesarios
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permisos = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.SEND_SMS
            };
        }

        // verifica si falta algun permisos
        boolean permisosFaltantes = false;
        for (String permiso : permisos) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                permisosFaltantes = true;
                break;
            }
        }

        // solicita los permisos si falta alguien
        if (permisosFaltantes) {
            ActivityCompat.requestPermissions(this, permisos, REQUEST_PERMISOS);
        }
    }
}
