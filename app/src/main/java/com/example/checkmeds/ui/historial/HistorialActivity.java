package com.example.checkmeds.ui.historial;

import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.checkmeds.R;
import com.example.checkmeds.data.CheckMedsDbHelper;
import com.example.checkmeds.data.Historial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Actividad que muestra la lista de los eventos registrados referente a la toma de medicamentos
// Carga el historial desde SQLite y crea una vista para los registros
public class HistorialActivity extends AppCompatActivity {

    // Contenedor donde se agregarán las vistas del historial
    private LinearLayout contenedorHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);

        // Enlazar vista principal
        contenedorHistorial = findViewById(R.id.contenedorHistorial);
        // Mostrar los registros de historial
        mostrarHistorial();
    }

    //Muestra el historial de medicamentos registrados
    private void mostrarHistorial() {
        CheckMedsDbHelper dbHelper = new CheckMedsDbHelper(this);
        List<Historial> historialList = dbHelper.obtenerHistorialCompleto();

        // Formato para mostrar la fecha y hora
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Recorre todos los registros del historial
        for (Historial h : historialList) {
            // Crear contenedor horizontal para cada ítem
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(24, 24, 24, 24);
            item.setBackgroundResource(R.drawable.bg_historial_item);
            item.setGravity(Gravity.CENTER_VERTICAL);

            // Definir margenes del ítem
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(16, 16, 16, 0);
            item.setLayoutParams(itemParams);

            // Contenedor vertical para los textos como Nombre, Estado, Fecha)
            LinearLayout textoLayout = new LinearLayout(this);
            textoLayout.setOrientation(LinearLayout.VERTICAL);
            textoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Nombre del medicamento
            TextView tvNombre = new TextView(this);
            tvNombre.setText(h.getNombreMedicamento());
            tvNombre.setTextSize(18);
            tvNombre.setTextColor(getResources().getColor(R.color.texto_titulo));
            tvNombre.setFontFeatureSettings("sans-serif-medium");
            textoLayout.addView(tvNombre);

            // Estado (Tomado, Pospuesto, Omitido)
            TextView tvEstado = new TextView(this);
            tvEstado.setText(h.getEstado());
            tvEstado.setTextSize(16);
            tvEstado.setFontFeatureSettings("sans-serif-medium");
            tvEstado.setPadding(0, 4, 0, 0);

            // Se asigna un color segun el estado de la toma del medicamento
            switch (h.getEstado().toLowerCase()) {
                case "tomado":
                    tvEstado.setTextColor(getResources().getColor(R.color.verde_estado));
                    break;
                case "pospuesto":
                    tvEstado.setTextColor(getResources().getColor(R.color.amarillo_estado));
                    break;
                case "omitido":
                    tvEstado.setTextColor(getResources().getColor(R.color.rojo_estado));
                    break;
                default:
                    tvEstado.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
            textoLayout.addView(tvEstado);

            // Fecha y hora
            TextView tvFecha = new TextView(this);
            tvFecha.setText(formato.format(new Date(h.getFechaHora())));
            tvFecha.setTextSize(14);
            tvFecha.setTextColor(getResources().getColor(R.color.texto_subtitulo));
            textoLayout.addView(tvFecha);

            item.addView(textoLayout);

            // Imagen de confirmación
            ImageView imgFoto = new ImageView(this);
            LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(150, 150);
            imgFoto.setLayoutParams(paramsImg);
            imgFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgFoto.setClipToOutline(true);
            //Define los bordes redondeados de laimagen
            imgFoto.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int radius = 24; // Bordes redondeados suaves
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });

            //Cargar la imagen si existe
            if (h.getFotoConfirmacion() != null && !h.getFotoConfirmacion().isEmpty()) {
                imgFoto.setImageURI(Uri.parse(h.getFotoConfirmacion()));
            } else {
                imgFoto.setImageResource(R.drawable.ic_camera_alt);
            }

            item.addView(imgFoto);

            // Acción al hacer clic en el item
            item.setOnClickListener(v -> {
                Intent intent = new Intent(HistorialActivity.this, DetalleHistorialActivity.class);
                intent.putExtra("nombre", h.getNombreMedicamento());
                intent.putExtra("estado", h.getEstado());
                intent.putExtra("fechaHora", formato.format(new Date(h.getFechaHora())));
                intent.putExtra("fotoUri", h.getFotoConfirmacion());
                startActivity(intent);
            });

            // Agregar ítem al contenedor principal
            contenedorHistorial.addView(item);
        }
    }
}
