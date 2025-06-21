package com.example.checkmeds.data;

// Clase Contacto para representar los datos de un contacto de confianza
public class Contacto {

    private int id;
    private String nombre;
    private String telefono;

    // Constructor vacío
    public Contacto() {
    }

    // Constructor con parámetros
    public Contacto(int id, String nombre, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    // Getter y Setter para id
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // Getter y Setter para nombre
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Getter y Setter para telefono
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}

