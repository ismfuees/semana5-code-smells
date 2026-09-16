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
 *   4. Nested Conditionals- condicionales anidados a 4 niveles en lugar de guard clauses
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

    // NOTA: los condicionales anidados (smell 4) se corrigen en la siguiente iteracion.
    public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
        // verificar que el estudiante no sea nulo
        if (estudiante != null) {
            // verificar que el horario no sea nulo
            if (horario != null) {
                if (horarioDisponible(horario)) {
                    if (cumpleAnticipacion(horasAnticipacion)) {
                        horario.reservar();
                        Reserva reserva = new Reserva(contadorId++, estudiante, horario);
                        repositorio.guardar(reserva);
                        estudiante.registrarReserva(reserva);
                        notificarCreacion(estudiante, reserva);
                        imprimirTicket(reserva, horario);
                        registrarAuditoria(reserva, horario);
                        return reserva;
                    }
                }
            }
        }
        return null;
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

    // verifica si se puede cancelar una reserva dada la anticipacion en horas
    public boolean puedeCancelar(Reserva reserva, int horasAnticipacion) {
        // una reserva nula no se puede cancelar
        if (reserva == null) {
            return false;
        }
        // si ya esta cancelada no aplica
        if (reserva.isCancelada()) {
            return false;
        }
        return cumpleAnticipacion(horasAnticipacion);
    }

    // cancela la reserva con el id dado si se cumplen las condiciones
    public void cancelarReserva(Long reservaId, int horasAnticipacion) {
        // buscar la reserva en el repositorio
        Reserva reserva = repositorio.buscarPorId(reservaId);
        // solo proceder si la reserva existe
        if (reserva != null) {
            // validar que se puede cancelar con la anticipacion indicada
            if (puedeCancelar(reserva, horasAnticipacion)) {
                reserva.cancelar();
                System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                        + ": Reserva " + reservaId + " cancelada.");
                System.out.println("AUDIT: reserva " + reservaId + " cancelada.");
            }
        }
    }

    // reprograma la reserva al nuevo horario indicado
    public void reprogramarReserva(Long reservaId, HorarioTutoria nuevoHorario, int horasAnticipacion) {
        // obtener la reserva del repositorio
        Reserva reserva = repositorio.buscarPorId(reservaId);
        // verificar que la reserva existe
        if (reserva != null) {
            // verificar que el nuevo horario no sea nulo
            if (nuevoHorario != null) {
                if (horarioDisponible(nuevoHorario)) {
                    if (cumpleAnticipacion(horasAnticipacion)) {
                        reserva.reprogramar(nuevoHorario);
                        repositorio.guardar(reserva);
                        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                                + ": Reserva " + reservaId + " reprogramada a horario " + nuevoHorario.getId());
                        System.out.println("AUDIT: reserva " + reservaId
                                + " reprogramada a horario " + nuevoHorario.getId());
                    }
                }
            }
        }
    }

    // confirma la reserva con el id indicado
    public void confirmarReserva(Long reservaId) {
        Reserva reserva = repositorio.buscarPorId(reservaId);
        if (reserva != null) {
            reserva.confirmar();
            System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                    + ": Reserva " + reservaId + " confirmada.");
        }
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
