package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.Reserva;

/**
 * Responsabilidad única: registrar eventos de auditoría e imprimir reportes.
 * <p>
 * Concentra toda la salida relacionada con trazabilidad del sistema:
 * el log de auditoría, el ticket de confirmación y el resumen del estudiante.
 */
public class AuditorReservas {

    public void registrarCreacion(Reserva reserva) {
        System.out.println("=== TICKET ===");
        System.out.println("Reserva   : " + reserva.getId());
        System.out.println("Estudiante: " + reserva.getEstudiante().getNombre());
        System.out.println("Horario   : " + reserva.getHorario().getId());
        System.out.println("Materia   : " + reserva.getHorario().getAsignatura().getNombre());
        System.out.println("==============");
        System.out.println("AUDIT: reserva " + reserva.getId()
                + " creada por " + reserva.getEstudiante().getNombre()
                + " en horario " + reserva.getHorario().getId());
    }

    public void registrarCancelacion(Reserva reserva) {
        System.out.println("AUDIT: reserva " + reserva.getId() + " cancelada.");
    }

    public void registrarReprogramacion(Reserva reserva) {
        System.out.println("AUDIT: reserva " + reserva.getId()
                + " reprogramada a horario " + reserva.getHorario().getId());
    }

    public void registrarConfirmacion(Reserva reserva) {
        System.out.println("AUDIT: reserva " + reserva.getId() + " confirmada.");
    }

    public void imprimirResumen(Estudiante estudiante) {
        System.out.println("--- Reservas de " + estudiante.getNombre() + " ---");
        for (Reserva reserva : estudiante.getReservas()) {
            System.out.println("  " + reserva);
        }
        System.out.println("----------------------------");
    }
}
