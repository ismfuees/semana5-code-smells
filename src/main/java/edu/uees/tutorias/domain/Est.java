package edu.uees.tutorias.domain;

import java.util.ArrayList;
import java.util.List;

// Estudiante del sistema de tutorias
public class Est extends U {

    // numero de matricula del estudiante
    private final String mat;
    private final List<Reserva> reservas = new ArrayList<>();

    public Est(Long id, String nombre, String email, String mat) {
        super(id, nombre, email);
        this.mat = mat;
    }

    // agrega la reserva a la lista del estudiante
    public void addR(Reserva r) {
        reservas.add(r);
    }

    // devuelve todas las reservas, sin proteger la lista
    public List<Reserva> getReservas() {
        return reservas;
    }

    public String getMat() { return mat; }
}
