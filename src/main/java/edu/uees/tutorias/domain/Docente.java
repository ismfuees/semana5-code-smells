package edu.uees.tutorias.domain;

import java.util.ArrayList;
import java.util.List;

public class Docente extends Usuario {

    private final String departamento;
    private final List<HorarioTutoria> horarios = new ArrayList<>();

    public Docente(Long id, String nombre, String email, String departamento) {
        super(id, nombre, email);
        this.departamento = departamento;
    }

    public void agregarHorario(HorarioTutoria horario) {
        horarios.add(horario);
    }

    public List<HorarioTutoria> getHorarios() {
        return horarios;
    }

    public String getDepartamento() { return departamento; }
}
