package com.example.checkmeds.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Clase para gestionar la base de datos local SQLite.
// CRUD para medicamentos, historial de tomas y contactos de confianza.
public class CheckMedsDbHelper extends SQLiteOpenHelper {

    // Nombre y versión de la Base de Datos
    public static final String DATABASE_NAME = "checkmeds.db";
    public static final int DATABASE_VERSION = 2; // se subió la version pues se modificó la base de datos

    // Constructor
    public CheckMedsDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Metodo llamado al crear la base de datos
    // se definen las estructuras de las tablas medicamento, historial y contacto
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla medicamentos
        db.execSQL(
                "CREATE TABLE medicamentos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL, " +
                        "dosis TEXT NOT NULL, " +
                        "frecuencia_horas INTEGER NOT NULL, " +
                        "dias_duracion INTEGER NOT NULL, " +
                        "fecha_inicio INTEGER NOT NULL, " +
                        "imagen_uri TEXT" +
                        ");"
        );

        // Crear tabla historial
        db.execSQL(
                "CREATE TABLE historial (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id_medicamento INTEGER NOT NULL, " +
                        "fecha_hora INTEGER NOT NULL, " +
                        "estado TEXT NOT NULL, " +
                        "foto_confirmacion TEXT, " +
                        "sms_enviado INTEGER DEFAULT 0, " +
                        "FOREIGN KEY (id_medicamento) REFERENCES medicamentos(id) ON DELETE CASCADE" +
                        ");"
        );

        // Crear tabla contacto
        db.execSQL(
                "CREATE TABLE contacto (" +
                        "id INTEGER PRIMARY KEY, " + // Solo 1 registro
                        "nombre TEXT NOT NULL, " +
                        "telefono TEXT NOT NULL" +
                        ");"
        );
    }

    // Metodo utilizado cuando se actualiza la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //se eliminan las tablas de la base de datos
        db.execSQL("DROP TABLE IF EXISTS historial");
        db.execSQL("DROP TABLE IF EXISTS medicamentos");
        db.execSQL("DROP TABLE IF EXISTS contacto");
        onCreate(db);
    }

    // CRUD para los registros de medicamentos
    // Inserta un nuevo medicamento en la base de datos parando un parametro Objeto Medicamento al insertar
    public long insertarMedicamento(Medicamento medicamento) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", medicamento.getNombre());
        valores.put("dosis", medicamento.getDosis());
        valores.put("frecuencia_horas", medicamento.getFrecuenciaHoras());
        valores.put("dias_duracion", medicamento.getDiasDuracion());
        valores.put("fecha_inicio", medicamento.getFechaInicio());
        valores.put("imagen_uri", medicamento.getImagenUri());
        long id = db.insert("medicamentos", null, valores);
        db.close();

        //se retorna el ID generado del medicamento registrado
        return id;
    }

    // Obtiene un medicamento en especifico por su ID
    public Medicamento obtenerMedicamentoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM medicamentos WHERE id = ?", new String[]{String.valueOf(id)});

        Medicamento med = null;
        if (cursor.moveToFirst()) {
            med = new Medicamento();
            med.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            med.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            med.setDosis(cursor.getString(cursor.getColumnIndexOrThrow("dosis")));
            med.setFrecuenciaHoras(cursor.getInt(cursor.getColumnIndexOrThrow("frecuencia_horas")));
            med.setDiasDuracion(cursor.getInt(cursor.getColumnIndexOrThrow("dias_duracion")));
            med.setFechaInicio(cursor.getLong(cursor.getColumnIndexOrThrow("fecha_inicio")));
            med.setImagenUri(cursor.getString(cursor.getColumnIndexOrThrow("imagen_uri")));
        }

        cursor.close();
        db.close();
        return med;
    }

    //Identifica en medicamento por si ID
    public void eliminarMedicamento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("medicamentos", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    //Se obtiene la lista de todos los medicamentos almacenados
    public List<Medicamento> obtenerTodosLosMedicamentos() {
        List<Medicamento> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM medicamentos", null);

        if (cursor.moveToFirst()) {
            do {
                Medicamento med = new Medicamento();
                med.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                med.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                med.setDosis(cursor.getString(cursor.getColumnIndexOrThrow("dosis")));
                med.setFrecuenciaHoras(cursor.getInt(cursor.getColumnIndexOrThrow("frecuencia_horas")));
                med.setDiasDuracion(cursor.getInt(cursor.getColumnIndexOrThrow("dias_duracion")));
                med.setFechaInicio(cursor.getLong(cursor.getColumnIndexOrThrow("fecha_inicio")));
                med.setImagenUri(cursor.getString(cursor.getColumnIndexOrThrow("imagen_uri")));
                lista.add(med);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    // Se actualiza la información de un medicamento registrado y retorna el numero de columnas afectadas
    public int actualizarMedicamento(Medicamento med) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", med.getNombre());
        values.put("dosis", med.getDosis());
        values.put("frecuencia_horas", med.getFrecuenciaHoras());
        values.put("dias_duracion", med.getDiasDuracion());
        values.put("fecha_inicio", med.getFechaInicio());
        values.put("imagen_uri", med.getImagenUri());
        return db.update("medicamentos", values, "id = ?", new String[]{String.valueOf(med.getId())});
    }

    // CRUD Historial

    //Inserta un nuevo registro en el historial con los parametros
    public void insertarHistorial(int idMedicamento, String estado, long fechaHora, String fotoUri, boolean smsEnviado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("id_medicamento", idMedicamento);
        valores.put("estado", estado);
        valores.put("fecha_hora", fechaHora);
        valores.put("foto_confirmacion", fotoUri);
        valores.put("sms_enviado", smsEnviado ? 1 : 0);
        db.insert("historial", null, valores);
        db.close();
    }

    //Se obtienen todods los registros del historial combinados con el nombre del medicamento
    // Retorna una lista de objetos
    public List<Historial> obtenerHistorialCompleto() {
        List<Historial> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT h.id, h.estado, h.fecha_hora, h.foto_confirmacion, m.nombre " +
                "FROM historial h INNER JOIN medicamentos m ON h.id_medicamento = m.id " +
                "ORDER BY h.fecha_hora DESC";

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Historial h = new Historial();
                h.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                h.setEstado(cursor.getString(cursor.getColumnIndexOrThrow("estado")));
                h.setNombreMedicamento(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                h.setFechaHora(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("fecha_hora"))));
                h.setFotoConfirmacion(cursor.getString(cursor.getColumnIndexOrThrow("foto_confirmacion")));
                lista.add(h);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    // CRUD Contacto

    // Insertar o actualizar contacto único
    // pasando parametros como nombre del contacto y el telefono
    public void insertarOActualizarContacto(String nombre, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 1); // Siempre ID 1
        values.put("nombre", nombre);
        values.put("telefono", telefono);
        db.insertWithOnConflict("contacto", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Obtener el contacto actual registrado
    public Contacto obtenerContacto() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, nombre, telefono FROM contacto WHERE id = 1", null);

        Contacto contacto = null;
        if (cursor.moveToFirst()) {
            contacto = new Contacto();
            contacto.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            contacto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            contacto.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow("telefono")));
        }

        cursor.close();
        db.close();
        return contacto;
    }

    // Obtener solo el teléfono del contacto registrado
    public String obtenerTelefonoContacto() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT telefono FROM contacto WHERE id = 1", null);

        String telefono = null;
        if (cursor.moveToFirst()) {
            telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"));
        }

        cursor.close();
        db.close();
        return telefono;
    }


}
