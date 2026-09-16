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
 *   2. Long Method        - crearReserva() realiza demasiadas responsabilidades en un unico metodo
 *   3. Duplicated Code    - la validacion de disponibilidad se repite en crearReserva() y reprogramarReserva()
 *   4. Nested Conditionals- condicionales anidados a 4 niveles en lugar de guard clauses
 *   5. Mixed Responsibilities - crearReserva() mezcla negocio, persistencia, notificacion y reporte
 *   6. Comments as Deodorant  - comentarios que compensan estructura poco clara
 */
public class ServicioReservas {

    private final RepositorioReservas repositorio;
    private final List<Docente> docentes = new ArrayList<>();
    private long contadorId = 1;

    public ServicioReservas(RepositorioReservas repositorio) {
        this.repositorio = repositorio;
    }

    public void agregarDocente(Docente docente) {
        docentes.add(docente);
    }

    // proceso principal: crea reserva, guarda, notifica y genera reporte
    // recibe un estudiante, un horario y la cantidad de horas de anticipacion
    public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
        // verificar que el estudiante no sea nulo
        if (estudiante != null) {
            // verificar que el horario no sea nulo
            if (horario != null) {
                // verificar que el horario este disponible para reservar
                if (horario.estaDisponible()) {
                    // verificar que se reserva con suficiente anticipacion (minimo 2 horas)
                    if (horasAnticipacion >= 2) {

                        // marcar el horario como ocupado
                        horario.reservar();

                        // crear el objeto reserva con el proximo id disponible
                        Reserva reserva = new Reserva(contadorId++, estudiante, horario);

                        // persistir la reserva en el repositorio
                        repositorio.guardar(reserva);

                        // asociar la reserva al estudiante para su historial
                        estudiante.registrarReserva(reserva);

                        // notificar al estudiante por correo electronico
                        System.out.println("EMAIL a " + estudiante.getEmail()
                                + ": Reserva creada. ID=" + reserva.getId());

                        // imprimir el ticket de confirmacion en consola
                        System.out.println("=== TICKET ===");
                        System.out.println("Reserva  : " + reserva.getId());
                        System.out.println("Estudiante: " + estudiante.getNombre());
                        System.out.println("Horario  : " + horario.getId());
                        System.out.println("Materia  : " + horario.getAsignatura().getNombre());
                        System.out.println("==============");

                        // generar el registro en el log de auditoria
                        System.out.println("AUDIT: reserva " + reserva.getId()
                                + " creada por " + estudiante.getNombre()
                                + " en horario " + horario.getId());

                        return reserva;
                    }
                }
            }
        }
        // si alguna validacion fallo, retornar nulo
        return null;
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
        // minimo 2 horas de anticipacion para cancelar
        if (horasAnticipacion < 2) {
            return false;
        }
        return true;
    }

    // cancela la reserva con el id dado si se cumplen las condiciones
    public void cancelarReserva(Long reservaId, int horasAnticipacion) {
        // buscar la reserva en el repositorio
        Reserva reserva = repositorio.buscarPorId(reservaId);
        // solo proceder si la reserva existe
        if (reserva != null) {
            // validar que se puede cancelar con la anticipacion indicada
            if (puedeCancelar(reserva, horasAnticipacion)) {
                // ejecutar la cancelacion en el dominio
                reserva.cancelar();
                // notificar al estudiante que su reserva fue cancelada
                System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                        + ": Reserva " + reservaId + " cancelada.");
                // registrar en log de auditoria
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
                // verificar que el nuevo horario este disponible para reservar
                // DUPLICADO: la misma validacion ya existe en crearReserva()
                if (nuevoHorario.estaDisponible()) {
                    // verificar anticipacion minima de 2 horas
                    // DUPLICADO: el mismo limite de 2 horas ya aparece en crearReserva() y puedeCancelar()
                    if (horasAnticipacion >= 2) {
                        // reprogramar en el dominio
                        reserva.reprogramar(nuevoHorario);
                        // persistir el cambio
                        repositorio.guardar(reserva);
                        // notificar al estudiante del cambio de horario
                        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                                + ": Reserva " + reservaId + " reprogramada a horario " + nuevoHorario.getId());
                        // log de auditoria del cambio
                        System.out.println("AUDIT: reserva " + reservaId
                                + " reprogramada a horario " + nuevoHorario.getId());
                    }
                }
            }
        }
    }

    // confirma la reserva con el id indicado
    public void confirmarReserva(Long reservaId) {
        // recuperar la reserva
        Reserva reserva = repositorio.buscarPorId(reservaId);
        // solo confirmar si existe
        if (reserva != null) {
            // confirmar en el dominio
            reserva.confirmar();
            // notificar al estudiante que la reserva fue confirmada
            System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                    + ": Reserva " + reservaId + " confirmada.");
        }
    }

    // imprime en consola el resumen de todas las reservas de un estudiante
    public void imprimirResumen(Estudiante estudiante) {
        // encabezado del resumen
        System.out.println("--- Reservas de " + estudiante.getNombre() + " ---");
        // iterar sobre todas las reservas del estudiante
        for (Reserva reserva : estudiante.getReservas()) {
            // imprimir cada reserva con su estado
            System.out.println("  " + reserva);
        }
        System.out.println("----------------------------");
    }
}
