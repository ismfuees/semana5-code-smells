package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Doc;
import edu.uees.tutorias.domain.Est;
import edu.uees.tutorias.domain.HorarioTutoria;
import edu.uees.tutorias.domain.Reserva;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio principal del sistema de tutorias.
 *
 * LINEA BASE - contiene code smells intencionales para la Kata de refactorizacion
 * de la Semana 5. NO modificar este archivo antes de realizar el diagnostico.
 *
 * Code smells presentes:
 *   1. Poor Naming        - nombres de clase (SR), metodos (proc, chk, p) y variables (r, e, h, x)
 *   2. Long Method        - proc() realiza demasiadas responsabilidades en un unico metodo
 *   3. Duplicated Code    - la validacion de disponibilidad se repite en proc() y reprog()
 *   4. Nested Conditionals- condicionales anidados a 4 niveles en lugar de guard clauses
 *   5. Mixed Responsibilities - proc() mezcla negocio, persistencia, notificacion y reporte
 *   6. Comments as Deodorant  - comentarios que compensan nombres y estructura poco claros
 */
public class SR {

    // repositorio donde se guardan las reservas
    private final Repo repo;
    // lista de todos los docentes del sistema
    private final List<Doc> docs = new ArrayList<>();
    // contador para generar ids de reservas
    private long x = 1;

    public SR(Repo repo) {
        this.repo = repo;
    }

    // agrega un docente al sistema
    public void addD(Doc d) {
        docs.add(d);
    }

    // proceso principal: crea reserva, guarda, notifica y genera reporte
    // recibe un estudiante, un horario y la cantidad de horas de anticipacion
    public Reserva proc(Est e, HorarioTutoria h, int ha) {
        // verificar que el estudiante no sea nulo
        if (e != null) {
            // verificar que el horario no sea nulo
            if (h != null) {
                // verificar que el horario este disponible para reservar
                if (h.estaDisponible()) {
                    // verificar que se reserva con suficiente anticipacion (minimo 2 horas)
                    if (ha >= 2) {

                        // marcar el horario como ocupado
                        h.reservar();

                        // crear el objeto reserva con el proximo id disponible
                        Reserva r = new Reserva(x++, e, h);

                        // persistir la reserva en el repositorio
                        repo.save(r);

                        // asociar la reserva al estudiante para su historial
                        e.addR(r);

                        // notificar al estudiante por correo electronico
                        System.out.println("EMAIL a " + e.getEmail()
                                + ": Reserva creada. ID=" + r.getId());

                        // imprimir el ticket de confirmacion en consola
                        System.out.println("=== TICKET ===");
                        System.out.println("Reserva  : " + r.getId());
                        System.out.println("Estudiante: " + e.getNombre());
                        System.out.println("Horario  : " + h.getId());
                        System.out.println("Materia  : " + h.getAsignatura().getNombre());
                        System.out.println("==============");

                        // generar el registro en el log de auditoría
                        System.out.println("AUDIT: reserva " + r.getId()
                                + " creada por " + e.getNombre()
                                + " en horario " + h.getId());

                        return r;
                    }
                }
            }
        }
        // si alguna validacion fallo, retornar nulo
        return null;
    }

    // verifica si se puede cancelar una reserva dada la anticipacion en horas
    public boolean chk(Reserva r, int h) {
        // una reserva nula no se puede cancelar
        if (r == null) {
            return false;
        }
        // si ya esta cancelada no aplica
        if (r.isCancelada()) {
            return false;
        }
        // minimo 2 horas de anticipacion para cancelar
        if (h < 2) {
            return false;
        }
        return true;
    }

    // cancela la reserva con el id dado si se cumplen las condiciones
    public void p(Long id, int ha) {
        // buscar la reserva en el repositorio
        Reserva r = repo.findById(id);
        // solo proceder si la reserva existe
        if (r != null) {
            // validar que se puede cancelar con la anticipacion indicada
            if (chk(r, ha)) {
                // ejecutar la cancelacion en el dominio
                r.cancelar();
                // notificar al estudiante que su reserva fue cancelada
                System.out.println("EMAIL a " + r.getEstudiante().getEmail()
                        + ": Reserva " + id + " cancelada.");
                // registrar en log de auditoria
                System.out.println("AUDIT: reserva " + id + " cancelada.");
            }
        }
    }

    // reprograma la reserva al nuevo horario indicado
    public void reprog(Long id, HorarioTutoria nh, int ha) {
        // obtener la reserva del repositorio
        Reserva r = repo.findById(id);
        // verificar que la reserva existe
        if (r != null) {
            // verificar que el nuevo horario no sea nulo
            if (nh != null) {
                // verificar que el nuevo horario este disponible para reservar
                // DUPLICADO: la misma validacion ya existe en proc()
                if (nh.estaDisponible()) {
                    // verificar anticipacion minima de 2 horas
                    // DUPLICADO: el mismo limite de 2 horas ya aparece en proc() y chk()
                    if (ha >= 2) {
                        // reprogramar en el dominio
                        r.reprogramar(nh);
                        // persistir el cambio
                        repo.save(r);
                        // notificar al estudiante del cambio de horario
                        System.out.println("EMAIL a " + r.getEstudiante().getEmail()
                                + ": Reserva " + id + " reprogramada a horario " + nh.getId());
                        // log de auditoria del cambio
                        System.out.println("AUDIT: reserva " + id
                                + " reprogramada a horario " + nh.getId());
                    }
                }
            }
        }
    }

    // confirma la reserva con el id indicado
    public void conf(Long id) {
        // recuperar la reserva
        Reserva r = repo.findById(id);
        // solo confirmar si existe
        if (r != null) {
            // confirmar en el dominio
            r.confirmar();
            // notificar al estudiante que la reserva fue confirmada
            System.out.println("EMAIL a " + r.getEstudiante().getEmail()
                    + ": Reserva " + id + " confirmada.");
        }
    }

    // imprime en consola el resumen de todas las reservas activas de un estudiante
    public void printR(Est e) {
        // encabezado del resumen
        System.out.println("--- Reservas de " + e.getNombre() + " ---");
        // iterar sobre todas las reservas del estudiante
        for (Reserva r : e.getReservas()) {
            // imprimir cada reserva con su estado
            System.out.println("  " + r);
        }
        System.out.println("----------------------------");
    }
}
