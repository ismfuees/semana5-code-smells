package edu.uees.tutorias.domain;

/**
 * Materia a la que corresponde la tutoria.
 */
public class Asignatura {

    private final Long id;
    private final String nombre;
    private final String codigo;

    public Asignatura(Long id, String nombre, String codigo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public Long getId()       { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }

    @Override
    public String toString() {
        return "Asignatura[" + codigo + " - " + nombre + "]";
    }
}
