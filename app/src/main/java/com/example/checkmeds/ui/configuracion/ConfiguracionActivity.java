package com.example.checkmeds.ui.configuracion;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.checkmeds.R;
import com.example.checkmeds.data.CheckMedsDbHelper;
import com.example.checkmeds.data.Medicamento;

import java.text.SimpleDateFormat;
import java.util.Locale;

//Actividad para configurar las opciones como activar o desactivas los SMS automaticos
//definir el tiempo para posponer las alarmas
//registra y / o actualiza el contacto
public class ConfiguracionActivity extends AppCompatActivity {

    // Elementos de la interfaz
    private EditText etTelefonoContacto, etTiempoPosponer;
    private Switch switchSms;
    private CheckMedsDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);

        // Inicializar base de datos
        dbHelper = new CheckMedsDbHelper(this);

        // Solicitar permiso para enviar SMS si no está otorgado
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 123);
        }

        // Asociar elementos de interfaz
        etTelefonoContacto = findViewById(R.id.etTelefonoContacto);
        switchSms = findViewById(R.id.switchSms);
        etTiempoPosponer = findViewById(R.id.etTiempoPosponer);
        Button btnGuardar = findViewById(R.id.btnGuardarConfig);

        // Cargar configuraciones previas
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        switchSms.setChecked(prefs.getBoolean("sms_activado", false));
        etTiempoPosponer.setText(String.valueOf(prefs.getInt("tiempo_posponer", 10)));

        // Cargar número de contacto guardado desde SQLite
        String telefonoGuardado = dbHelper.obtenerTelefonoContacto();
        if (telefonoGuardado != null) {
            etTelefonoContacto.setText(telefonoGuardado);
        }

        // Botón guardar configuración
        btnGuardar.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            // Guardar estado del switch de SMS
            editor.putBoolean("sms_activado", switchSms.isChecked());

            // Guardar tiempo de posponer
            try {
                int minutos = Integer.parseInt(etTiempoPosponer.getText().toString().trim());
                editor.putInt("tiempo_posponer", minutos);
            } catch (NumberFormatException e) {
                editor.putInt("tiempo_posponer", 10);  // Valor por defecto
            }
            editor.apply();

            // Guardar o actualizar contacto en SQLite
            String telefonoNuevo = etTelefonoContacto.getText().toString().trim();
            if (!telefonoNuevo.isEmpty()) {
                dbHelper.insertarOActualizarContacto("Contacto de confianza", telefonoNuevo);
                Toast.makeText(this, "Configuración guardada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ingrese un número de contacto válido", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de SMS concedido", Toast.LENGTH_SHORT).show();
        }
    }

    //Envia un SMS desde fuera de la actividad para notificar el estado del mediamento
    public static void enviarSmsEstadoDesdeFuera(Context context, Medicamento medicamento, String estado) {
        SharedPreferences prefs = context.getSharedPreferences("config", MODE_PRIVATE);
        boolean smsActivado = prefs.getBoolean("sms_activado", false);

        if (!smsActivado) return; // Si SMS no está activado, salir

        CheckMedsDbHelper dbHelper = new CheckMedsDbHelper(context);
        String telefono = dbHelper.obtenerTelefonoContacto();

        if (telefono != null && !telefono.isEmpty()) {
            // Crear mensaje SMS
            String mensaje = "CheckMeds - Medicamento: " + medicamento.getNombre() +
                    "\nEstado: " + estado +
                    "\nFecha y hora: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(System.currentTimeMillis());

            // Enviar SMS
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(telefono, null, mensaje, null, null);
        }
    }
}
