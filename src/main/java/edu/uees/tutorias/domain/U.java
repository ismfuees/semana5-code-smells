package edu.uees.tutorias.domain;

// Clase base de usuarios del sistema
public abstract class U {

    // identificador
    private final Long id;
    // nombre completo
    private final String n;
    // correo electronico
    private final String e;

    protected U(Long id, String n, String e) {
        this.id = id;
        this.n = n;
        this.e = e;
    }

    public Long getId() { return id; }
    public String getNombre() { return n; }
    public String getEmail() { return e; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", nombre=" + n + "]";
    }
}
