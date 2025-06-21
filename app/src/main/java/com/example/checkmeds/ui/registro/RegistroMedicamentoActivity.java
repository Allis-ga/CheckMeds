package com.example.checkmeds.ui.registro;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.checkmeds.R;
import com.example.checkmeds.data.CheckMedsDbHelper;
import com.example.checkmeds.data.Medicamento;
import com.example.checkmeds.util.AlarmUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// acatividad para registrar o editar un medicamento
public class RegistroMedicamentoActivity extends AppCompatActivity {

    // vistas de la interfaz
    private EditText etNombre, etDosis, etFrecuencia, etDuracion, etFechaInicio;
    private ImageView imgMedicamento;
    private Uri uriImagenSeleccionada;

    // variables de apoyo
    private Calendar fechaSeleccionada;

    private Button btnGuardar, btnSeleccionarImagen;
    private CheckMedsDbHelper dbHelper;
    private static final int REQUEST_TOMAR_FOTO = 1;

    //modo edicion
    private boolean enModoEdicion = false;
    private int idMedicamentoEditar = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_medicamento);

        dbHelper = new CheckMedsDbHelper(this);
        fechaSeleccionada = Calendar.getInstance();

        // Asociar vistas
        etNombre = findViewById(R.id.etNombre);
        etDosis = findViewById(R.id.etDosis);
        etFrecuencia = findViewById(R.id.etFrecuencia);
        etDuracion = findViewById(R.id.etDuracion);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        imgMedicamento = findViewById(R.id.imgMedicamento);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);

        // Verificar si es modo edición
        enModoEdicion = getIntent().getBooleanExtra("modo_edicion", false);
        if (enModoEdicion) {
            idMedicamentoEditar = getIntent().getIntExtra("id_medicamento", -1);
            if (idMedicamentoEditar != -1) {
                Medicamento med = dbHelper.obtenerMedicamentoPorId(idMedicamentoEditar);
                if (med != null) {
                    cargarDatosParaEditar(med);
                }
            }
        }

        // Selector de fecha y hora
        etFechaInicio.setOnClickListener(v -> mostrarSelectorFechaHora());

        // Botón para seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> abrirCamara());

        btnGuardar.setOnClickListener(view -> {
            String nombre = etNombre.getText().toString().trim();
            String dosis = etDosis.getText().toString().trim();
            int frecuencia = Integer.parseInt(etFrecuencia.getText().toString().trim());
            int duracion = Integer.parseInt(etDuracion.getText().toString().trim());
            long fechaInicio = fechaSeleccionada.getTimeInMillis();
            String imagenUri = uriImagenSeleccionada != null ? uriImagenSeleccionada.toString() : null;

            Medicamento nuevoMed = new Medicamento();
            nuevoMed.setNombre(nombre);
            nuevoMed.setDosis(dosis);
            nuevoMed.setFrecuenciaHoras(frecuencia);
            nuevoMed.setDiasDuracion(duracion);
            nuevoMed.setFechaInicio(fechaInicio);
            nuevoMed.setImagenUri(imagenUri);

            long resultado;

            if (enModoEdicion) {
                nuevoMed.setId(idMedicamentoEditar);
                resultado = dbHelper.actualizarMedicamento(nuevoMed);

                if (resultado != -1) {
                    Toast.makeText(this, "Medicamento actualizado", Toast.LENGTH_LONG).show();

                    // Cancelar todas las alarmas anteriores y reprogramar nuevas
                    AlarmUtils.cancelarAlarmasPorMedicamento(this, nuevoMed.getId());

                    AlarmUtils.programarAlarmasPorFrecuencia(
                            this,
                            nuevoMed.getId(),
                            nuevoMed.getFechaInicio(),
                            nuevoMed.getFrecuenciaHoras(),
                            nuevoMed.getDiasDuracion()
                    );
                    finish();
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_LONG).show();
                }

            } else {
                resultado = dbHelper.insertarMedicamento(nuevoMed);

                if (resultado != -1) {
                    Toast.makeText(this, "Medicamento guardado correctamente", Toast.LENGTH_LONG).show();

                    AlarmUtils.programarAlarmasPorFrecuencia(
                            this,
                            (int) resultado,
                            nuevoMed.getFechaInicio(),
                            nuevoMed.getFrecuenciaHoras(),
                            nuevoMed.getDiasDuracion()
                    );
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Carga datos existentes al editar un medicamento
    private void cargarDatosParaEditar(Medicamento med) {
        etNombre.setText(med.getNombre());
        etDosis.setText(med.getDosis());
        etFrecuencia.setText(String.valueOf(med.getFrecuenciaHoras()));
        etDuracion.setText(String.valueOf(med.getDiasDuracion()));

        fechaSeleccionada.setTimeInMillis(med.getFechaInicio());
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        etFechaInicio.setText(formato.format(fechaSeleccionada.getTime()));

        if (med.getImagenUri() != null) {
            uriImagenSeleccionada = Uri.parse(med.getImagenUri());
            imgMedicamento.setImageURI(uriImagenSeleccionada);
        }
    }

    // muestra un selector de fehca y hora
    private void mostrarSelectorFechaHora() {
        final Calendar calendarioActual = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    fechaSeleccionada.set(Calendar.YEAR, year);
                    fechaSeleccionada.set(Calendar.MONTH, month);
                    fechaSeleccionada.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                fechaSeleccionada.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                fechaSeleccionada.set(Calendar.MINUTE, minute);
                                fechaSeleccionada.set(Calendar.SECOND, 0);

                                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                etFechaInicio.setText(formato.format(fechaSeleccionada.getTime()));
                            },
                            calendarioActual.get(Calendar.HOUR_OF_DAY),
                            calendarioActual.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendarioActual.get(Calendar.YEAR),
                calendarioActual.get(Calendar.MONTH),
                calendarioActual.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    //Abre la camara para capturaar una imagen
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File fotoArchivo;
            try {
                fotoArchivo = crearArchivoImagen();
            } catch (IOException ex) {
                Toast.makeText(this, "No se pudo crear archivo para la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fotoArchivo != null) {
                uriImagenSeleccionada = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        fotoArchivo
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagenSeleccionada);
                startActivityForResult(intent, REQUEST_TOMAR_FOTO);
            }
        }
    }

    //Crea un archivo temporal para almacenar la imegen capturada
    private File crearArchivoImagen() throws IOException {
        String nombreArchivo = "foto_medicamento_" + System.currentTimeMillis();
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombreArchivo, ".jpg", directorio);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TOMAR_FOTO && resultCode == RESULT_OK) {
            imgMedicamento.setImageURI(uriImagenSeleccionada);
        }
    }
}
