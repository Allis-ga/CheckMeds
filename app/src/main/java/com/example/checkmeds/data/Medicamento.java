package com.example.checkmeds.data;

//Modelo de datos para representar un medicamento registrado incluyento toda la informacion

public class Medicamento {

    //atributos privados del medicamento
    private int id;
    private String nombre;
    private String dosis;
    private int frecuenciaHoras;
    private int diasDuracion;
    private long fechaInicio;
    private String imagenUri;

    //constructor vacío
    public Medicamento() {}


    // contructor completo
    // tiene parametros com ID, nombre medicamento, dosis, horas, dias, decha de inicio del tratamiento, imagen Uri
    public Medicamento(int id, String nombre, String dosis, int frecuenciaHoras, int diasDuracion, long fechaInicio, String imagenUri) {
        this.id = id;
        this.nombre = nombre;
        this.dosis = dosis;
        this.frecuenciaHoras = frecuenciaHoras;
        this.diasDuracion = diasDuracion;
        this.fechaInicio = fechaInicio;
        this.imagenUri = imagenUri;
    }

    //Obtiene el ID del medicamento
    public int getId() { return id; }
    //establece el ID del medicamento
    public void setId(int id) { this.id = id; }

    //obtiene el nombre del medicamento
    public String getNombre() {
        return nombre;
    }
    //establece el nombre del medicamento
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    //Obtiene la dosis a administrar
    public String getDosis() {
        return dosis;
    }

    //Establece la dosis
    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    //Obtiene la frecuencia de administracion en horas
    public int getFrecuenciaHoras() {
        return frecuenciaHoras;
    }

    //establece la frecuencia de administracion en horas
    public void setFrecuenciaHoras(int frecuenciaHoras) {
        this.frecuenciaHoras = frecuenciaHoras;
    }

    //Obtiene la duracion del tratamiento en dias
    public int getDiasDuracion() {
        return diasDuracion;
    }
    public void setDiasDuracion(int diasDuracion) {
        this.diasDuracion = diasDuracion;
    }

    // obtiene la fecha de inicio del tratamiento
    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    //Establece la Uro de la imagen asociada al medicamento
    public String getImagenUri() {
        return imagenUri;
    }
    public void setImagenUri(String imagenUri) {
        this.imagenUri = imagenUri;
    }

    //calcla el timestamp de la proxima alarma basada en la frecuencia de administracoin
    public long calcularProximaAlarma() {
        long ahora = System.currentTimeMillis();
        long siguiente = fechaInicio;

        // Avanza en intervalos hasta que la proxima alarma sea futura
        while (siguiente < ahora) {
            siguiente += frecuenciaHoras * 60 * 60 * 1000L; // Sumar frecuencia en milisegundos
        }

        // Calcular la fecha de finalizacio del tratamiento
        long finTratamiento = fechaInicio + (diasDuracion * 24L * 60 * 60 * 1000L);
        return (siguiente <= finTratamiento) ? siguiente : -1; // retorna -1 si ya terminó
    }

}
