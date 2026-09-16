package edu.uees.tutorias.domain;

public abstract class Usuario {

    private final Long id;
    private final String nombre;
    private final String email;

    protected Usuario(Long id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public Long getId()       { return id; }
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", nombre=" + nombre + "]";
    }
}
