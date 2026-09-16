package edu.uees.tutorias.domain;

import java.util.ArrayList;
import java.util.List;

public class Estudiante extends Usuario {

    private final String matricula;
    private final List<Reserva> reservas = new ArrayList<>();

    public Estudiante(Long id, String nombre, String email, String matricula) {
        super(id, nombre, email);
        this.matricula = matricula;
    }

    public void registrarReserva(Reserva reserva) {
        reservas.add(reserva);
    }

    // devuelve todas las reservas, sin proteger la lista
    public List<Reserva> getReservas() {
        return reservas;
    }

    public String getMatricula() { return matricula; }
}
