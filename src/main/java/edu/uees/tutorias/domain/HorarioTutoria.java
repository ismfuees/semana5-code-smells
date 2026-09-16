package edu.uees.tutorias.domain;

import java.time.LocalDateTime;

// Representa un slot de tiempo publicado por un docente
public class HorarioTutoria {

    private final Long id;
    // fecha y hora de inicio
    private final LocalDateTime i;
    // fecha y hora de fin
    private final LocalDateTime f;
    private final Asignatura asignatura;
    // true si nadie lo ha reservado todavia
    private boolean disp;

    public HorarioTutoria(Long id, LocalDateTime i, LocalDateTime f, Asignatura asignatura) {
        this.id = id;
        this.i = i;
        this.f = f;
        this.asignatura = asignatura;
        // el horario siempre empieza disponible
        this.disp = true;
    }

    // marca como ocupado
    public void reservar() {
        if (!disp) {
            throw new IllegalStateException("Horario ya ocupado.");
        }
        this.disp = false;
    }

    // libera el horario cuando se cancela
    public void liberar() {
        this.disp = true;
    }

    public boolean estaDisponible() { return disp; }

    public Long getId()             { return id; }
    public LocalDateTime getInicio(){ return i; }
    public LocalDateTime getFin()   { return f; }
    public Asignatura getAsignatura(){ return asignatura; }

    @Override
    public String toString() {
        return "HorarioTutoria[id=" + id + ", " + i + " -> " + f
                + ", " + asignatura.getNombre() + ", disponible=" + disp + "]";
    }
}
