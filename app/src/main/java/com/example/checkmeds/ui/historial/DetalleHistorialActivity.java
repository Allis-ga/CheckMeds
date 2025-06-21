package com.example.checkmeds.ui.historial;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.checkmeds.R;


//Actividad que muestra el detalle del registro en el historial
public class DetalleHistorialActivity extends AppCompatActivity {

    // Vistas de la interfaz
    private ImageView imgConfirmacion;
    private TextView tvNombre, tvEstado, tvFechaHora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_historial);

        // Enlazar los componentes con sus id correspondiente
        imgConfirmacion = findViewById(R.id.imgConfirmacionDetalle);
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvEstado = findViewById(R.id.tvEstadoDetalle);
        tvFechaHora = findViewById(R.id.tvFechaHoraDetalle);

        // Recibir los datos enviados desde HistorialActivity
        String nombre = getIntent().getStringExtra("nombre");
        String estado = getIntent().getStringExtra("estado");
        String fechaHora = getIntent().getStringExtra("fechaHora");
        String fotoUri = getIntent().getStringExtra("fotoUri");

        // Mostrar datos recibidos
        tvNombre.setText(nombre);
        tvEstado.setText(estado);
        tvFechaHora.setText(fechaHora);

        // Cambiar color del estado
        if (estado.equalsIgnoreCase("Tomado")) {
            tvEstado.setTextColor(getResources().getColor(R.color.verde_estado));
        } else if (estado.equalsIgnoreCase("Pospuesto")) {
            tvEstado.setTextColor(getResources().getColor(R.color.amarillo_estado));
        } else if (estado.equalsIgnoreCase("Omitido")) {
            tvEstado.setTextColor(getResources().getColor(R.color.rojo_estado));
        }

        // Mostrar la foto de confirmacion o un ícono si no hay imagen
        if (fotoUri != null && !fotoUri.isEmpty()) {
            imgConfirmacion.setImageURI(Uri.parse(fotoUri));
        } else {
            imgConfirmacion.setImageResource(R.drawable.ic_camera_alt);
        }

    }
}