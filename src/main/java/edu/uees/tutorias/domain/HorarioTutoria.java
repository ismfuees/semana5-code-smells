package edu.uees.tutorias.domain;

import java.time.LocalDateTime;

public class HorarioTutoria {

    private final Long id;
    private final LocalDateTime inicio;
    private final LocalDateTime fin;
    private final Asignatura asignatura;
    // true si nadie lo ha reservado todavia
    private boolean disponible;

    public HorarioTutoria(Long id, LocalDateTime inicio, LocalDateTime fin, Asignatura asignatura) {
        this.id = id;
        this.inicio = inicio;
        this.fin = fin;
        this.asignatura = asignatura;
        this.disponible = true;
    }

    // marca como ocupado
    public void reservar() {
        if (!disponible) {
            throw new IllegalStateException("Horario ya ocupado.");
        }
        this.disponible = false;
    }

    // libera el horario cuando se cancela
    public void liberar() {
        this.disponible = true;
    }

    public boolean estaDisponible()    { return disponible; }

    public Long getId()                { return id; }
    public LocalDateTime getInicio()   { return inicio; }
    public LocalDateTime getFin()      { return fin; }
    public Asignatura getAsignatura()  { return asignatura; }

    @Override
    public String toString() {
        return "HorarioTutoria[id=" + id + ", " + inicio + " -> " + fin
                + ", " + asignatura.getNombre() + ", disponible=" + disponible + "]";
    }
}
