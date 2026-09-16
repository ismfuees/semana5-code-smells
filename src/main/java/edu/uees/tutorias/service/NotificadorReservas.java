package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Reserva;

/**
 * Responsabilidad única: notificar al estudiante sobre cambios en sus reservas.
 * <p>
 * En esta implementación las notificaciones se imprimen en consola.
 * Si en el futuro se requiere enviar correos reales, solo se cambia esta clase.
 */
public class NotificadorReservas {

    public void notificarCreacion(Reserva reserva) {
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva creada. ID=" + reserva.getId());
    }

    public void notificarCancelacion(Reserva reserva) {
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva " + reserva.getId() + " cancelada.");
    }

    public void notificarReprogramacion(Reserva reserva) {
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva " + reserva.getId()
                + " reprogramada a horario " + reserva.getHorario().getId());
    }

    public void notificarConfirmacion(Reserva reserva) {
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva " + reserva.getId() + " confirmada.");
    }
}
