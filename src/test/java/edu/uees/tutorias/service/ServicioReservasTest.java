package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Asignatura;
import edu.uees.tutorias.domain.Docente;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.EstadoReserva;
import edu.uees.tutorias.domain.HorarioTutoria;
import edu.uees.tutorias.domain.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba unitaria de ServicioReservas — linea base de comportamiento observable.
 *
 * Cada prueba verifica un escenario concreto. Antes de refactorizar,
 * todas deben pasar. Despues de refactorizar, todas deben seguir pasando.
 */
class ServicioReservasTest {

    private ServicioReservas servicio;
    private Estudiante estudiante;
    private HorarioTutoria horario;

    @BeforeEach
    void setUp() {
        servicio = new ServicioReservas(new RepositorioReservas());

        estudiante = new Estudiante(1L, "Ana Lopez", "ana@uees.edu.ec", "2024-001");

        Docente docente = new Docente(10L, "Maria Lopez", "mlopez@uees.edu.ec", "Ciencias");
        servicio.agregarDocente(docente);

        Asignatura asignatura = new Asignatura(1L, "Diseno de Software", "UCOM0310");
        horario = new HorarioTutoria(
                100L,
                LocalDateTime.of(2026, 8, 20, 9, 0),
                LocalDateTime.of(2026, 8, 20, 10, 0),
                asignatura
        );
        docente.agregarHorario(horario);
    }

    @Test
    void crearReserva_conDatosValidos_debeOcuparHorarioYRetornarReserva() {
        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);

        assertNotNull(reserva);
        assertFalse(horario.estaDisponible());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void crearReserva_conAnticipacionInsuficiente_debeRetornarNulo() {
        Reserva reserva = servicio.crearReserva(estudiante, horario, 1);

        assertNull(reserva);
        assertTrue(horario.estaDisponible());
    }

    @Test
    void crearReserva_conEstudianteNulo_debeRetornarNulo() {
        Reserva reserva = servicio.crearReserva(null, horario, 3);

        assertNull(reserva);
        assertTrue(horario.estaDisponible());
    }

    @Test
    void confirmarReserva_reservaExistente_debeCambiarEstadoAConfirmada() {
        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);
        servicio.confirmarReserva(reserva.getId());

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void cancelarReserva_conAnticipacionSuficiente_debeCancelarYLiberarHorario() {
        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);
        servicio.cancelarReserva(reserva.getId(), 4);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertTrue(horario.estaDisponible());
    }

    @Test
    void cancelarReserva_conAnticipacionInsuficiente_noDebeCancelar() {
        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);
        servicio.cancelarReserva(reserva.getId(), 1);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void reprogramarReserva_aHorarioDisponible_debeLiberarHorarioAnteriorYOcuparNuevo() {
        Asignatura asignatura2 = new Asignatura(2L, "Programacion II", "CC201");
        HorarioTutoria nuevoHorario = new HorarioTutoria(
                101L,
                LocalDateTime.of(2026, 8, 21, 10, 0),
                LocalDateTime.of(2026, 8, 21, 11, 0),
                asignatura2
        );

        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);
        servicio.reprogramarReserva(reserva.getId(), nuevoHorario, 4);

        assertTrue(horario.estaDisponible());
        assertFalse(nuevoHorario.estaDisponible());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void reprogramarReserva_aHorarioNoDisponible_noDebeReprogramar() {
        Asignatura asignatura2 = new Asignatura(2L, "Programacion II", "CC201");
        HorarioTutoria nuevoHorario = new HorarioTutoria(
                101L,
                LocalDateTime.of(2026, 8, 21, 10, 0),
                LocalDateTime.of(2026, 8, 21, 11, 0),
                asignatura2
        );
        nuevoHorario.reservar();

        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);
        Long idHorarioAnterior = reserva.getHorario().getId();
        servicio.reprogramarReserva(reserva.getId(), nuevoHorario, 4);

        assertEquals(idHorarioAnterior, reserva.getHorario().getId());
    }

    @Test
    void puedeCancelar_reservaValidaConAnticipacionSuficiente_debeRetornarTrue() {
        Reserva reserva = servicio.crearReserva(estudiante, horario, 3);

        assertTrue(servicio.puedeCancelar(reserva, 2));
    }

    @Test
    void puedeCancelar_reservaNula_debeRetornarFalse() {
        assertFalse(servicio.puedeCancelar(null, 5));
    }

    @Test
    void crearReserva_debeRegistrarLaReservaEnElHistorialDelEstudiante() {
        servicio.crearReserva(estudiante, horario, 3);

        assertEquals(1, estudiante.getReservas().size());
    }
}
