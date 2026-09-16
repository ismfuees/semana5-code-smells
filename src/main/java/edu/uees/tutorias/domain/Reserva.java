package edu.uees.tutorias.domain;

import java.time.LocalDateTime;

public class Reserva {

    private final Long id;
    private final Estudiante estudiante;
    private HorarioTutoria horario;
    private EstadoReserva estado;
    // fecha en que se creo la reserva
    private final LocalDateTime fechaCreacion;

    public Reserva(Long id, Estudiante estudiante, HorarioTutoria horario) {
        this.id = id;
        this.estudiante = estudiante;
        this.horario = horario;
        this.estado = EstadoReserva.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    // confirma la reserva si esta pendiente
    public void confirmar() {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException(
                "Solo se puede confirmar una reserva PENDIENTE. Estado: " + estado);
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    // cancela la reserva y libera el horario
    public void cancelar() {
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.REALIZADA) {
            throw new IllegalStateException("No se puede cancelar. Estado: " + estado);
        }
        this.estado = EstadoReserva.CANCELADA;
        this.horario.liberar();
    }

    // reprograma a otro horario
    public void reprogramar(HorarioTutoria nuevoHorario) {
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.REALIZADA) {
            throw new IllegalStateException("No se puede reprogramar. Estado: " + estado);
        }
        this.horario.liberar();
        nuevoHorario.reservar();
        this.horario = nuevoHorario;
        this.estado = EstadoReserva.PENDIENTE;
    }

    // marca como realizada cuando la tutoria ocurrio
    public void marcarRealizada() {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                "Solo se puede marcar realizada una reserva CONFIRMADA. Estado: " + estado);
        }
        this.estado = EstadoReserva.REALIZADA;
    }

    public Long getId()                  { return id; }
    public Estudiante getEstudiante()    { return estudiante; }
    public HorarioTutoria getHorario()   { return horario; }
    public EstadoReserva getEstado()     { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public boolean isCancelada() { return estado == EstadoReserva.CANCELADA; }

    @Override
    public String toString() {
        return "Reserva[id=" + id + ", estado=" + estado
                + ", estudiante=" + estudiante.getNombre()
                + ", horario=" + horario.getId() + "]";
    }
}
