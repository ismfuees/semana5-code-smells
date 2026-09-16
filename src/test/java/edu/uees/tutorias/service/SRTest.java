package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Asignatura;
import edu.uees.tutorias.domain.Doc;
import edu.uees.tutorias.domain.Est;
import edu.uees.tutorias.domain.EstadoReserva;
import edu.uees.tutorias.domain.HorarioTutoria;
import edu.uees.tutorias.domain.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba unitaria de SR — linea base de comportamiento observable.
 *
 * Cada prueba verifica un escenario concreto. Antes de refactorizar,
 * todas deben pasar. Despues de refactorizar, todas deben seguir pasando.
 */
class SRTest {

    // variables de instancia con nombres poco expresivos (smell: Poor Naming)
    private SR s;
    private Est e;
    private HorarioTutoria h;

    @BeforeEach
    void setUp() {
        s = new SR(new Repo());

        e = new Est(1L, "Ana Lopez", "ana@uees.edu.ec", "2024-001");

        Doc d = new Doc(10L, "Maria Lopez", "mlopez@uees.edu.ec", "Ciencias");
        s.addD(d);

        Asignatura a = new Asignatura(1L, "Diseno de Software", "UCOM0310");
        h = new HorarioTutoria(
                100L,
                LocalDateTime.of(2026, 8, 20, 9, 0),
                LocalDateTime.of(2026, 8, 20, 10, 0),
                a
        );
        d.addH(h);
    }

    // crear reserva con anticipacion suficiente: el horario debe quedar ocupado
    @Test
    void proc_conDatosValidos_debeOcuparHorarioYRetornarReserva() {
        Reserva r = s.proc(e, h, 3);

        assertNotNull(r);
        assertFalse(h.estaDisponible());
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    // crear reserva con anticipacion insuficiente: debe retornar nulo sin cambiar el horario
    @Test
    void proc_conAnticipacionInsuficiente_debeRetornarNulo() {
        Reserva r = s.proc(e, h, 1);

        assertNull(r);
        assertTrue(h.estaDisponible());
    }

    // crear reserva con estudiante nulo: debe retornar nulo
    @Test
    void proc_conEstudianteNulo_debeRetornarNulo() {
        Reserva r = s.proc(null, h, 3);

        assertNull(r);
        assertTrue(h.estaDisponible());
    }

    // confirmar una reserva existente: el estado debe cambiar a CONFIRMADA
    @Test
    void conf_reservaExistente_debeCambiarEstadoAConfirmada() {
        Reserva r = s.proc(e, h, 3);
        s.conf(r.getId());

        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    // cancelar con anticipacion suficiente: el horario debe quedar libre
    @Test
    void p_conAnticipacionSuficiente_debeCancelarYLiberarHorario() {
        Reserva r = s.proc(e, h, 3);
        s.p(r.getId(), 4);

        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertTrue(h.estaDisponible());
    }

    // cancelar con anticipacion insuficiente: la reserva no debe cambiar de estado
    @Test
    void p_conAnticipacionInsuficiente_noDebeCancelar() {
        Reserva r = s.proc(e, h, 3);
        s.p(r.getId(), 1);

        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    // reprogramar a un nuevo horario disponible: el horario original debe liberarse
    @Test
    void reprog_aHorarioDisponible_debeLiberarHorarioAnteriorYOcuparNuevo() {
        Asignatura a2 = new Asignatura(2L, "Programacion II", "CC201");
        HorarioTutoria nh = new HorarioTutoria(
                101L,
                LocalDateTime.of(2026, 8, 21, 10, 0),
                LocalDateTime.of(2026, 8, 21, 11, 0),
                a2
        );

        Reserva r = s.proc(e, h, 3);
        s.reprog(r.getId(), nh, 4);

        assertTrue(h.estaDisponible());
        assertFalse(nh.estaDisponible());
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    // reprogramar a un horario no disponible: nada debe cambiar
    @Test
    void reprog_aHorarioNoDisponible_noDebeReprogramar() {
        Asignatura a2 = new Asignatura(2L, "Programacion II", "CC201");
        HorarioTutoria nh = new HorarioTutoria(
                101L,
                LocalDateTime.of(2026, 8, 21, 10, 0),
                LocalDateTime.of(2026, 8, 21, 11, 0),
                a2
        );
        nh.reservar(); // ocupar el nuevo horario de antemano

        Reserva r = s.proc(e, h, 3);
        Long idAnterior = r.getH().getId();
        s.reprog(r.getId(), nh, 4);

        // el horario original no debe haberse liberado
        assertEquals(idAnterior, r.getH().getId());
    }

    // chk: reserva valida con anticipacion suficiente debe devolver true
    @Test
    void chk_reservaValidaConAnticipacionSuficiente_debeRetornarTrue() {
        Reserva r = s.proc(e, h, 3);

        assertTrue(s.chk(r, 2));
    }

    // chk: reserva nula debe devolver false
    @Test
    void chk_reservaNula_debeRetornarFalse() {
        assertFalse(s.chk(null, 5));
    }

    // el estudiante debe tener la reserva registrada en su historial
    @Test
    void proc_debeRegistrarLaReservaEnElHistorialDelEstudiante() {
        s.proc(e, h, 3);

        assertEquals(1, e.getReservas().size());
    }
}
