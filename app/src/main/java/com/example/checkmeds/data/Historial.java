package com.example.checkmeds.data;

import java.util.Date;

//Representa un registro en el historial de tomas de medicamentos que
// incluye la informacion sobre el medicamento, el estado de la toma, fecha y hora y la foto de confirmacion
public class Historial {

    //atributos del historial
    private int id;
    private int idMedicamento;
    private long fechaHora;
    private String estado;
    private String fotoConfirmacion;
    private boolean smsEnviado;
    private String nombreMedicamento;

    //constructor
    public Historial() {}

    // obtiene el ID del registro del historial y lo retorna
    public int getId() {
        return id;
    }
    // establece el ID de registro de historial tomando com parametro el ID del historial
    public void setId(int id) {
        this.id = id;
    }

    //Obtiene fecha y hora del evento para establecer la alarma
    public long getFechaHora() {
        return fechaHora;
    }

    //Establece la fecha y hora por un Objeto Date
    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora.getTime();
    }

    //Obtiene el estado de la toma - Tomado, pospuesto u omitido
    public String getEstado() {
        return estado;
    }

    //Establece el estado de la toma del medicamento
    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Obtiene la URO de la foto tomada como 'confirmacion'
    public String getFotoConfirmacion() {
        return fotoConfirmacion;
    }

    //Establece la URI de la foto de la confirmación
    public void setFotoConfirmacion(String fotoConfirmacion) {
        this.fotoConfirmacion = fotoConfirmacion;
    }

    //Obtiene el nombre del medicamento asociado y lo retorna
    public String getNombreMedicamento() {
        return nombreMedicamento;
    }

    //Establece el nombre del medicamento asociado tomando como parametro nombreMedicamento
    public void setNombreMedicamento(String nombreMedicamento) {
        this.nombreMedicamento = nombreMedicamento;
    }
}
