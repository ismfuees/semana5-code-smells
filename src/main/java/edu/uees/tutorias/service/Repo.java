package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Reserva;

import java.util.HashMap;
import java.util.Map;

// guarda y recupera reservas en memoria
public class Repo {

    // mapa de id a reserva
    private final Map<Long, Reserva> data = new HashMap<>();

    public void save(Reserva r) {
        data.put(r.getId(), r);
    }

    public Reserva findById(Long id) {
        return data.get(id);
    }
}
