package edu.uees.tutorias.domain;

/**
 * Materia a la que corresponde la tutoria.
 */
public class Asignatura {

    private final Long id;
    private final String nombre;
    // codigo de la materia en el sistema academico
    private final String c;

    public Asignatura(Long id, String nombre, String c) {
        this.id = id;
        this.nombre = nombre;
        this.c = c;
    }

    public Long getId()      { return id; }
    public String getNombre() { return nombre; }
    // retorna el codigo
    public String getCodigo() { return c; }

    @Override
    public String toString() {
        return "Asignatura[" + c + " - " + nombre + "]";
    }
}
