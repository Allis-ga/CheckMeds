package com.example.checkmeds.ui.registro;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.checkmeds.R;
import com.example.checkmeds.data.CheckMedsDbHelper;
import com.example.checkmeds.data.Medicamento;
import com.example.checkmeds.util.AlarmUtils;

import com.example.checkmeds.R;

//Muestra la información detallada de un medicamento
public class DetalleMedicamentoActivity extends AppCompatActivity {

    // Vistas de la interfaz
    private TextView tvNombre, tvDosis, tvFrecuencia, tvDuracion;
    private ImageView imgMedicamento;
    private Button btnEliminar, btnEditar;

    // Datos del medicamento y base de datos
    private Medicamento medicamento;
    private CheckMedsDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_medicamento);

        // Asociar vistas
        tvNombre = findViewById(R.id.tvNombre);
        tvDosis = findViewById(R.id.tvDosis);
        tvFrecuencia = findViewById(R.id.tvFrecuencia);
        tvDuracion = findViewById(R.id.tvDuracion);
        imgMedicamento = findViewById(R.id.imgMedicamento);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnEditar = findViewById(R.id.btnEditar);

        // Inicializar base de datos
        dbHelper = new CheckMedsDbHelper(this);

        // Obtener el ID del medicamento enviado en el Intent
        int idMedicamento = getIntent().getIntExtra("id_medicamento", -1);
        if (idMedicamento != -1) {
            medicamento = dbHelper.obtenerMedicamentoPorId(idMedicamento);
            if (medicamento != null) {
                mostrarDatos();
            }
        }

        // botón eliminar medicamento
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoConfirmacion();
            }
        });

        // boton editar medicamento
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetalleMedicamentoActivity.this, RegistroMedicamentoActivity.class);
                intent.putExtra("modo_edicion", true);
                intent.putExtra("id_medicamento", medicamento.getId());
                startActivity(intent);
                finish();
            }
        });


    }

    // Muestra todos los datos del medicamento en la interfaz
    private void mostrarDatos() {
        tvNombre.setText(medicamento.getNombre());
        tvDosis.setText("Dosis a tomar: " + medicamento.getDosis());
        tvFrecuencia.setText("Cada " + medicamento.getFrecuenciaHoras() + " h");
        tvDuracion.setText("Por " + medicamento.getDiasDuracion() + " días");

        if (medicamento.getImagenUri() != null && !medicamento.getImagenUri().isEmpty()) {
            imgMedicamento.setImageURI(Uri.parse(medicamento.getImagenUri()));
        } else {
            imgMedicamento.setImageResource(R.drawable.ic_medication);
        }
    }

    //Muestra un diálogo de confirmación para eliminar el medicamento.
    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Medicamento")
                .setMessage("¿Estás seguro de que deseas eliminar este medicamento?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlarmUtils.cancelarAlarmasPorMedicamento(
                                DetalleMedicamentoActivity.this,
                                medicamento.getId()
                        );

                        dbHelper.eliminarMedicamento(medicamento.getId());
                        Toast.makeText(DetalleMedicamentoActivity.this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}