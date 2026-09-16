package edu.uees.tutorias.domain;

import java.time.LocalDateTime;

// Representa la reserva de una tutoria por parte de un estudiante
public class Reserva {

    private final Long id;
    private final Est e;      // el estudiante que reservo
    private HorarioTutoria h; // el horario reservado
    private EstadoReserva estado;
    // fecha en que se creo la reserva
    private final LocalDateTime fc;

    public Reserva(Long id, Est e, HorarioTutoria h) {
        this.id = id;
        this.e = e;
        this.h = h;
        this.estado = EstadoReserva.PENDIENTE;
        this.fc = LocalDateTime.now();
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
        this.h.liberar();
    }

    // reprograma a otro horario
    public void reprogramar(HorarioTutoria nh) {
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.REALIZADA) {
            throw new IllegalStateException("No se puede reprogramar. Estado: " + estado);
        }
        this.h.liberar();
        nh.reservar();
        this.h = nh;
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

    public Long getId()           { return id; }
    public Est getEstudiante()    { return e; }
    public HorarioTutoria getH()  { return h; }
    public EstadoReserva getEstado() { return estado; }
    public LocalDateTime getFc()  { return fc; }

    public boolean isCancelada()  { return estado == EstadoReserva.CANCELADA; }

    @Override
    public String toString() {
        return "Reserva[id=" + id + ", estado=" + estado
                + ", estudiante=" + e.getNombre()
                + ", horario=" + h.getId() + "]";
    }
}
