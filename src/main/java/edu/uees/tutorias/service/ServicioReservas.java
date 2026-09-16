package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Docente;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioTutoria;
import edu.uees.tutorias.domain.Reserva;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilidad única: orquestar la lógica de negocio de reservas de tutoría.
 * <p>
 * No envía notificaciones ni genera reportes directamente.
 * Esas responsabilidades pertenecen a NotificadorReservas y AuditorReservas.
 */
public class ServicioReservas {

    private static final int HORAS_MINIMAS_ANTICIPACION = 2;

    private final RepositorioReservas repositorio;
    private final NotificadorReservas notificador;
    private final AuditorReservas auditor;
    private final List<Docente> docentes = new ArrayList<>();
    private long contadorId = 1;

    public ServicioReservas(RepositorioReservas repositorio) {
        this(repositorio, new NotificadorReservas(), new AuditorReservas());
    }

    public ServicioReservas(RepositorioReservas repositorio,
                            NotificadorReservas notificador,
                            AuditorReservas auditor) {
        this.repositorio = repositorio;
        this.notificador = notificador;
        this.auditor = auditor;
    }

    public void agregarDocente(Docente docente) {
        docentes.add(docente);
    }

    public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
        if (estudiante == null)                     return null;
        if (horario == null)                        return null;
        if (!horarioDisponible(horario))            return null;
        if (!cumpleAnticipacion(horasAnticipacion)) return null;

        horario.reservar();
        Reserva reserva = new Reserva(contadorId++, estudiante, horario);
        repositorio.guardar(reserva);
        estudiante.registrarReserva(reserva);
        notificador.notificarCreacion(reserva);
        auditor.registrarCreacion(reserva);
        return reserva;
    }

    public boolean puedeCancelar(Reserva reserva, int horasAnticipacion) {
        if (reserva == null)       return false;
        if (reserva.isCancelada()) return false;
        return cumpleAnticipacion(horasAnticipacion);
    }

    public void cancelarReserva(Long reservaId, int horasAnticipacion) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva == null)                            return;
        if (!puedeCancelar(reserva, horasAnticipacion)) return;

        reserva.cancelar();
        notificador.notificarCancelacion(reserva);
        auditor.registrarCancelacion(reserva);
    }

    public void reprogramarReserva(Long reservaId, HorarioTutoria nuevoHorario, int horasAnticipacion) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva == null)                        return;
        if (nuevoHorario == null)                   return;
        if (!horarioDisponible(nuevoHorario))       return;
        if (!cumpleAnticipacion(horasAnticipacion)) return;

        reserva.reprogramar(nuevoHorario);
        repositorio.guardar(reserva);
        notificador.notificarReprogramacion(reserva);
        auditor.registrarReprogramacion(reserva);
    }

    public void confirmarReserva(Long reservaId) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva == null) return;

        reserva.confirmar();
        notificador.notificarConfirmacion(reserva);
        auditor.registrarConfirmacion(reserva);
    }

    public void imprimirResumen(Estudiante estudiante) {
        auditor.imprimirResumen(estudiante);
    }

    private boolean horarioDisponible(HorarioTutoria horario) {
        return horario.estaDisponible();
    }

    private boolean cumpleAnticipacion(int horasAnticipacion) {
        return horasAnticipacion >= HORAS_MINIMAS_ANTICIPACION;
    }
}
