package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Docente;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioTutoria;
import edu.uees.tutorias.domain.Reserva;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio principal del sistema de tutorias.
 *
 * Code smells presentes (pendientes de refactorizacion):
 *   5. Mixed Responsibilities - crearReserva() mezcla negocio, persistencia, notificacion y reporte
 *   6. Comments as Deodorant  - comentarios que compensan estructura poco clara
 */
public class ServicioReservas {

    private static final int HORAS_MINIMAS_ANTICIPACION = 2;

    private final RepositorioReservas repositorio;
    private final List<Docente> docentes = new ArrayList<>();
    private long contadorId = 1;

    public ServicioReservas(RepositorioReservas repositorio) {
        this.repositorio = repositorio;
    }

    public void agregarDocente(Docente docente) {
        docentes.add(docente);
    }

    public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
        if (estudiante == null)                return null;
        if (horario == null)                   return null;
        if (!horarioDisponible(horario))       return null;
        if (!cumpleAnticipacion(horasAnticipacion)) return null;

        horario.reservar();
        Reserva reserva = new Reserva(contadorId++, estudiante, horario);
        repositorio.guardar(reserva);
        estudiante.registrarReserva(reserva);
        notificarCreacion(estudiante, reserva);
        imprimirTicket(reserva, horario);
        registrarAuditoria(reserva, horario);
        return reserva;
    }

    private boolean horarioDisponible(HorarioTutoria horario) {
        return horario.estaDisponible();
    }

    private boolean cumpleAnticipacion(int horasAnticipacion) {
        return horasAnticipacion >= HORAS_MINIMAS_ANTICIPACION;
    }

    private void notificarCreacion(Estudiante estudiante, Reserva reserva) {
        System.out.println("EMAIL a " + estudiante.getEmail()
                + ": Reserva creada. ID=" + reserva.getId());
    }

    private void imprimirTicket(Reserva reserva, HorarioTutoria horario) {
        System.out.println("=== TICKET ===");
        System.out.println("Reserva  : " + reserva.getId());
        System.out.println("Estudiante: " + reserva.getEstudiante().getNombre());
        System.out.println("Horario  : " + horario.getId());
        System.out.println("Materia  : " + horario.getAsignatura().getNombre());
        System.out.println("==============");
    }

    private void registrarAuditoria(Reserva reserva, HorarioTutoria horario) {
        System.out.println("AUDIT: reserva " + reserva.getId()
                + " creada por " + reserva.getEstudiante().getNombre()
                + " en horario " + horario.getId());
    }

    public boolean puedeCancelar(Reserva reserva, int horasAnticipacion) {
        if (reserva == null)       return false;
        if (reserva.isCancelada()) return false;
        return cumpleAnticipacion(horasAnticipacion);
    }

    public void cancelarReserva(Long reservaId, int horasAnticipacion) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva == null)                         return;
        if (!puedeCancelar(reserva, horasAnticipacion)) return;

        reserva.cancelar();
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva " + reservaId + " cancelada.");
        System.out.println("AUDIT: reserva " + reservaId + " cancelada.");
    }

    public void reprogramarReserva(Long reservaId, HorarioTutoria nuevoHorario, int horasAnticipacion) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva == null)                       return;
        if (nuevoHorario == null)                  return;
        if (!horarioDisponible(nuevoHorario))      return;
        if (!cumpleAnticipacion(horasAnticipacion)) return;

        reserva.reprogramar(nuevoHorario);
        repositorio.guardar(reserva);
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva " + reservaId + " reprogramada a horario " + nuevoHorario.getId());
        System.out.println("AUDIT: reserva " + reservaId
                + " reprogramada a horario " + nuevoHorario.getId());
    }

    public void confirmarReserva(Long reservaId) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva == null) return;

        reserva.confirmar();
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva " + reservaId + " confirmada.");
    }

    // imprime en consola el resumen de todas las reservas de un estudiante
    public void imprimirResumen(Estudiante estudiante) {
        System.out.println("--- Reservas de " + estudiante.getNombre() + " ---");
        for (Reserva reserva : estudiante.getReservas()) {
            System.out.println("  " + reserva);
        }
        System.out.println("----------------------------");
    }
}
