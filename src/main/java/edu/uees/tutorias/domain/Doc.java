package edu.uees.tutorias.domain;

import java.util.ArrayList;
import java.util.List;

// Docente que publica horarios de tutoria
public class Doc extends U {

    // departamento al que pertenece el docente
    private final String dep;
    private final List<HorarioTutoria> hs = new ArrayList<>();

    public Doc(Long id, String nombre, String email, String dep) {
        super(id, nombre, email);
        this.dep = dep;
    }

    // agrega un horario disponible
    public void addH(HorarioTutoria h) {
        hs.add(h);
    }

    // retorna todos los horarios
    public List<HorarioTutoria> getHs() {
        return hs;
    }

    public String getDep() { return dep; }
}
