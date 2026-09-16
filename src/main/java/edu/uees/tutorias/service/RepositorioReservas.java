package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Reserva;

import java.util.HashMap;
import java.util.Map;

public class RepositorioReservas {

    private final Map<Long, Reserva> almacen = new HashMap<>();

    public void guardar(Reserva reserva) {
        almacen.put(reserva.getId(), reserva);
    }

    public Reserva buscarPorId(Long id) {
        return almacen.get(id);
    }
}
