# Ae4 – Kata de Refactorización: Antes y Después

## Información general

- **Universidad:** Universidad Espíritu Santo
- **Carrera:** Computación
- **Asignatura:** Diseño de Software
- **Código:** UCOM0310
- **Periodo:** PEL 4 - 2026
- **Estudiante:** IVAN STALYN MUELA FLOR
- **Docente:** Ph.D. Jaime Paul Sayago Heredia

---

## 1. Objetivo de la actividad

Aplicar un proceso completo de refactorización sobre código Java existente: identificar Code Smells, seleccionar las técnicas pertinentes, realizar cambios incrementales verificados por pruebas unitarias y documentar la evolución antes/después sin alterar el comportamiento observable del sistema.

---

## 2. Descripción del código inicial

El proyecto `semana5-code-smells` implementa un sistema de gestión de tutorías académicas. Permite a estudiantes reservar horarios publicados por docentes, confirmar, cancelar y reprogramar reservas.

**Estructura de paquetes (línea base `c545037`):**

```
src/main/java/edu/uees/tutorias/
├── domain/
│   ├── U.java            ← clase base de usuarios (nombre débil)
│   ├── Est.java          ← estudiante (nombre débil)
│   ├── Doc.java          ← docente (nombre débil)
│   ├── Asignatura.java
│   ├── HorarioTutoria.java
│   ├── Reserva.java
│   └── EstadoReserva.java
└── service/
    ├── Repo.java         ← repositorio en memoria (nombre débil)
    └── SR.java           ← servicio principal (concentra todos los smells)

src/test/java/edu/uees/tutorias/service/
└── SRTest.java           ← prueba unitaria (11 casos)
```

**Estructura actual (tras refactorizaciones 1 y 2):**

```
src/main/java/edu/uees/tutorias/
├── domain/
│   ├── Usuario.java
│   ├── Estudiante.java
│   ├── Docente.java
│   ├── Asignatura.java
│   ├── HorarioTutoria.java
│   ├── Reserva.java
│   └── EstadoReserva.java
└── service/
    ├── RepositorioReservas.java
    └── ServicioReservas.java   ← guard clauses, sin anidamiento

src/test/java/edu/uees/tutorias/service/
└── ServicioReservasTest.java   ← mismos 11 casos, sin cambios
```

---

## 3. Línea base de comportamiento

**Comando de verificación:**
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn test
```

**Resultado en la línea base (`c545037`):**
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Commit de línea base:**
```
c545037 — Initial: registrar linea base con code smells intencionales
```

Los 11 casos de prueba definen el comportamiento observable que debe preservarse en todos los commits:

| # | Caso de prueba |
|---|---|
| 1 | `crearReserva` con datos válidos ocupa el horario y retorna reserva en estado `PENDIENTE` |
| 2 | `crearReserva` con anticipación < 2 h retorna `null` sin tocar el horario |
| 3 | `crearReserva` con estudiante `null` retorna `null` sin tocar el horario |
| 4 | `confirmarReserva` cambia el estado a `CONFIRMADA` |
| 5 | `cancelarReserva` con anticipación suficiente cancela y libera el horario |
| 6 | `cancelarReserva` con anticipación < 2 h no cancela |
| 7 | `reprogramarReserva` a horario disponible libera el anterior y ocupa el nuevo |
| 8 | `reprogramarReserva` a horario no disponible no modifica nada |
| 9 | `puedeCancelar` con reserva válida y anticipación ≥ 2 h → `true` |
| 10 | `puedeCancelar` con reserva `null` → `false` |
| 11 | `crearReserva` registra la reserva en el historial del estudiante |

---

## 4. Matriz de Code Smells identificados

| Código / ubicación | Smell | Evidencia | Impacto |
|---|---|---|---|
| Clases `U`, `Est`, `Doc`, `SR`, `Repo`; métodos `proc()`, `chk()`, `p()`, `conf()`, `reprog()`; variables `e`, `h`, `ha`, `r`, `x`, `n`, `fc`, `i`, `f`, `disp`, `mat`, `dep`, `hs`, `c` | **Poor Naming** | Ningún nombre expresa el dominio. `proc()` no comunica que crea una reserva; `x` es el contador de IDs. | El lector debe deducir la intención de cada símbolo. En trabajo colaborativo, cada desarrollador nuevo paga ese costo. |
| `SR.proc()` — 40 líneas que realizan validación, ocupar horario, crear objeto, persistir, registrar en estudiante, enviar email, imprimir ticket y generar log de auditoría en un único bloque | **Long Method** | 8 responsabilidades distintas dentro de un sólo método. No es posible nombrar ningún bloque interno porque no hay separación. | Cualquier cambio en la notificación o el ticket obliga a tocar el mismo método que tiene la lógica de negocio. El tamaño dificulta leer, entender y modificar con seguridad. |
| La expresión `h.estaDisponible()` y la condición `ha >= 2` aparecen literalmente en `proc()` y en `reprog()` | **Duplicated Code** | Misma regla de disponibilidad y mismo umbral de 2 horas copiados en dos métodos. | Cambiar la regla (p. ej. 3 horas mínimas) exige modificar ambos lugares. Si se olvida uno, el sistema aplica la regla en un flujo y no en otro. |
| `proc()` tiene 4 niveles de anidamiento: `if (e != null) { if (h != null) { if (h.estaDisponible()) { if (ha >= 2) { ... } } } }` | **Nested Conditionals** | El flujo principal está enterrado al fondo de cuatro niveles. | Cada nivel adicional eleva la carga cognitiva necesaria para entender cuándo se ejecuta la lógica real. |
| `proc()` mezcla: lógica de negocio (`reservar()`), persistencia (`repo.save()`), notificación (`EMAIL`), impresión de ticket (`=== TICKET ===`) y log de auditoría (`AUDIT:`) | **Mixed Responsibilities** | Cinco preocupaciones distintas en un único método. | Una clase con responsabilidades mezcladas tiene muchas razones para cambiar. Cambiar el formato del ticket no debería implicar tocar la lógica de negocio. |
| Comentarios como `// verificar que el estudiante no sea nulo` o `// marcar el horario como ocupado` — cada línea tiene un comentario que describe lo que el nombre ya debería decir | **Comments as Deodorant** | Los comentarios son más largos que el código que describen. Existen sólo porque los nombres no comunican intención. | Los comentarios se desactualizan; el código no. Un comentario que miente es peor que ninguno. |

---

## 5. Plan de refactorización

| Prioridad | Problema | Técnica | Justificación |
|---|---|---|---|
| 1 ✅ | **Poor Naming** — clases, métodos, variables y parámetros sin expresión de dominio | **Rename** | Es el cambio de menor riesgo (sólo léxico). Se realiza primero para que los commits posteriores sean legibles. |
| 2 ✅ | **Long Method** — `crearReserva()` hace demasiadas cosas en un sólo bloque | **Extract Method** | Separar las responsabilidades en métodos con nombre claro reduce el tamaño del método principal y hace visible cada intención. |
| 3 ✅ | **Duplicated Code** — la misma regla de disponibilidad y el mismo umbral de horas copiados en dos métodos | **Extract Method** + **constante** | Unificar la regla en un único lugar garantiza que cualquier cambio futuro se aplique en todo el sistema a la vez. |
| 4 ✅ | **Nested Conditionals** — condicionales anidados a 4 niveles en `crearReserva()` y `reprogramarReserva()` | **Guard Clauses** | Retornar temprano cuando no se cumple una condición elimina el anidamiento y deja el camino principal visible sin indentación. |
| 5 ✅ | **Mixed Responsibilities** — `ServicioReservas` mezcla negocio, notificaciones y auditoría en los mismos métodos | **Extract Class** | Mover cada responsabilidad a su propia clase hace que cada clase tenga una única razón para cambiar. |
| 6 ✅ | **Comments as Deodorant** — comentarios que repiten lo que el nombre del método o campo ya comunica | **Remove Comment** | Si el nombre es suficientemente claro, el comentario no aporta información y puede desactualizarse. Basta con eliminarlo. |

---

## 6. Refactorización 1 — Poor Naming → Rename

### Problema

Toda la base de código usa nombres de una o dos letras. El lector debe reconstruir mentalmente la intención de cada símbolo sin ninguna ayuda del código.

### Antes (`c545037`) — fragmento representativo

```java
public class SR {
    private final Repo repo;
    private long x = 1;

    public Reserva proc(Est e, HorarioTutoria h, int ha) { ... }
    public boolean chk(Reserva r, int h) { ... }
    public void p(Long id, int ha) { ... }
    public void conf(Long id) { ... }
    public void reprog(Long id, HorarioTutoria nh, int ha) { ... }
    public void printR(Est e) { ... }
}

public abstract class U {
    private final String n;
    private final String e;
}
```

### Cambio aplicado

| Antes | Después |
|---|---|
| Clase `U` | `Usuario` |
| Clase `Est` | `Estudiante` |
| Clase `Doc` | `Docente` |
| Clase `SR` | `ServicioReservas` |
| Clase `Repo` | `RepositorioReservas` |
| `U.n`, `U.e` | `nombre`, `email` |
| `Est.mat`, `getMat()`, `addR()` | `matricula`, `getMatricula()`, `registrarReserva()` |
| `Doc.dep`, `hs`, `getDep()`, `addH()`, `getHs()` | `departamento`, `horarios`, `getDepartamento()`, `agregarHorario()`, `getHorarios()` |
| `Asignatura.c` | `codigo` |
| `HorarioTutoria.i`, `f`, `disp` | `inicio`, `fin`, `disponible` |
| `Reserva.e`, `h`, `fc`, `getH()`, `getFc()` | `estudiante`, `horario`, `fechaCreacion`, `getHorario()`, `getFechaCreacion()` |
| `SR.x`, `addD()`, `proc()`, `chk()`, `p()`, `conf()`, `reprog()`, `printR()` | `contadorId`, `agregarDocente()`, `crearReserva()`, `puedeCancelar()`, `cancelarReserva()`, `confirmarReserva()`, `reprogramarReserva()`, `imprimirResumen()` |
| Parámetros `e`, `h`, `ha`, `r`, `d`, `nh` | `estudiante`, `horario`, `horasAnticipacion`, `reserva`, `docente`, `nuevoHorario` |

### Después (`6192265`) — fragmento

```java
public class ServicioReservas {
    private final RepositorioReservas repositorio;
    private long contadorId = 1;

    public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) { ... }
    public boolean puedeCancelar(Reserva reserva, int horasAnticipacion) { ... }
    public void cancelarReserva(Long reservaId, int horasAnticipacion) { ... }
    public void confirmarReserva(Long reservaId) { ... }
    public void reprogramarReserva(Long reservaId, HorarioTutoria nuevoHorario, int horasAnticipacion) { ... }
    public void imprimirResumen(Estudiante estudiante) { ... }
}
```

### Verificación

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

### Commit

```
6192265 — refactor: corregir Poor Naming en todo el proyecto
```

---

## 7. Refactorización 2 — Long Method → Extract Method

### Problema

El método `crearReserva()` tenía 8 responsabilidades en un sólo bloque de ~45 líneas:
ocupar el horario, crear el objeto, persistirlo, registrarlo en el estudiante, enviar el email de notificación, imprimir el ticket de confirmación y escribir el log de auditoría. Cambiar cualquiera de esos pasos obligaba a leer y tocar todo el método.

### Antes (`6192265`) — método completo

```java
public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
    if (estudiante != null) {
        if (horario != null) {
            if (horario.estaDisponible()) {
                if (horasAnticipacion >= 2) {
                    horario.reservar();
                    Reserva reserva = new Reserva(contadorId++, estudiante, horario);
                    repositorio.guardar(reserva);
                    estudiante.registrarReserva(reserva);

                    // bloque notificacion — 2 líneas
                    System.out.println("EMAIL a " + estudiante.getEmail()
                            + ": Reserva creada. ID=" + reserva.getId());

                    // bloque ticket — 6 líneas
                    System.out.println("=== TICKET ===");
                    System.out.println("Reserva  : " + reserva.getId());
                    System.out.println("Estudiante: " + estudiante.getNombre());
                    System.out.println("Horario  : " + horario.getId());
                    System.out.println("Materia  : " + horario.getAsignatura().getNombre());
                    System.out.println("==============");

                    // bloque auditoria — 3 líneas
                    System.out.println("AUDIT: reserva " + reserva.getId()
                            + " creada por " + estudiante.getNombre()
                            + " en horario " + horario.getId());

                    return reserva;
                }
            }
        }
    }
    return null;
}
```

### Cambio aplicado

Se identificaron tres bloques con intención clara y nombrable:

| Bloque extraído | Nuevo método privado | Responsabilidad |
|---|---|---|
| `System.out.println("EMAIL …")` | `notificarCreacion(estudiante, reserva)` | Avisar al estudiante que su reserva fue creada |
| Bloque `=== TICKET ===` | `imprimirTicket(reserva, horario)` | Mostrar el resumen de la reserva en consola |
| `System.out.println("AUDIT …")` | `registrarAuditoria(reserva, horario)` | Dejar constancia del evento en el log |

### Después (`b8c7689`) — método principal y métodos extraídos

```java
public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
    if (estudiante != null) {
        if (horario != null) {
            if (horario.estaDisponible()) {
                if (horasAnticipacion >= 2) {
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
```

### Qué mejoró / qué NO cambió

- **Qué mejoró:** `crearReserva()` bajó de ~45 líneas a 12. Cada llamada en el método principal comunica exactamente qué hace ese paso. Los tres bloques extraídos pueden modificarse de forma independiente sin tocar la lógica de negocio.
- **Qué NO cambió:** La reserva sigue creándose, guardándose, notificándose, imprimiendo el ticket y registrando la auditoría exactamente igual que antes. El comportamiento observable es idéntico.

### Verificación

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

### Commit

```
b8c7689  refactor: extraer metodos notificarCreacion, imprimirTicket y registrarAuditoria de crearReserva
```

---

## 8. Refactorización 3 — Duplicated Code → Extract Method + Constante

### Problema

La regla "el horario debe estar disponible" aparecía escrita de forma idéntica en dos métodos distintos (`crearReserva` y `reprogramarReserva`):

```java
if (horario.estaDisponible()) { ... }      // en crearReserva()
if (nuevoHorario.estaDisponible()) { ... } // en reprogramarReserva()
```

Lo mismo ocurría con el umbral de anticipación mínima: el número literal `2` aparecía tres veces en el código — en `crearReserva`, en `reprogramarReserva` y con signo contrario en `puedeCancelar`:

```java
if (horasAnticipacion >= 2) { ... }  // en crearReserva()
if (horasAnticipacion >= 2) { ... }  // en reprogramarReserva()
if (horasAnticipacion < 2)  { ... }  // en puedeCancelar()
```

El problema: si la política académica cambia ese mínimo a 3 horas, hay que buscar todos los lugares donde aparece `2` y actualizarlos. Si se olvida uno, el sistema aplica reglas distintas dependiendo de qué operación se ejecute.

### Antes (`b8c7689`) — fragmentos duplicados

```java
// En crearReserva():
if (horario.estaDisponible()) {
    if (horasAnticipacion >= 2) {
        ...
    }
}

// En reprogramarReserva() — copia exacta:
if (nuevoHorario.estaDisponible()) {   // DUPLICADO
    if (horasAnticipacion >= 2) {      // DUPLICADO
        ...
    }
}

// En puedeCancelar() — misma regla con signo contrario:
if (horasAnticipacion < 2) {           // DUPLICADO
    return false;
}
```

### Cambio aplicado

Se extrajeron dos métodos privados y una constante que concentran la regla en un único lugar:

| Antes (duplicado) | Después (un sólo lugar) |
|---|---|
| `horario.estaDisponible()` repetido 2 veces | Método privado `horarioDisponible(horario)` |
| Literal `2` repetido 3 veces | Constante `HORAS_MINIMAS_ANTICIPACION = 2` |
| `horasAnticipacion >= 2` / `< 2` repetido 3 veces | Método privado `cumpleAnticipacion(horasAnticipacion)` |

### Después (`9aeb870`) — código unificado

```java
private static final int HORAS_MINIMAS_ANTICIPACION = 2;

private boolean horarioDisponible(HorarioTutoria horario) {
    return horario.estaDisponible();
}

private boolean cumpleAnticipacion(int horasAnticipacion) {
    return horasAnticipacion >= HORAS_MINIMAS_ANTICIPACION;
}

// crearReserva() — usa los métodos extraídos:
if (horarioDisponible(horario)) {
    if (cumpleAnticipacion(horasAnticipacion)) { ... }
}

// reprogramarReserva() — usa los mismos métodos:
if (horarioDisponible(nuevoHorario)) {
    if (cumpleAnticipacion(horasAnticipacion)) { ... }
}

// puedeCancelar() — también usa el mismo método:
return cumpleAnticipacion(horasAnticipacion);
```

Ahora cambiar el umbral de 2 a 3 horas sólo requiere modificar un número en un único lugar: la constante `HORAS_MINIMAS_ANTICIPACION`.

### Qué mejoró / qué NO cambió

- **Qué mejoró:** la regla de anticipación mínima y la verificación de disponibilidad viven cada una en un único lugar. Cualquier cambio futuro se aplica automáticamente en todos los flujos.
- **Qué NO cambió:** el comportamiento del sistema es idéntico. Si el horario no está disponible o la anticipación es menor a 2 horas, la operación no se realiza, igual que antes.

### Verificación

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

### Commit

```
9aeb870  refactor: extraer horarioDisponible y cumpleAnticipacion para eliminar codigo duplicado
```

---

## 9. Refactorización 4 — Nested Conditionals → Guard Clauses

### Problema

`crearReserva()` tenía 4 niveles de `if` anidados. La lógica principal estaba enterrada en el fondo de la pirámide y era imposible leer cuándo se ejecutaba cada paso sin contar llaves manualmente:

```java
if (estudiante != null) {
    if (horario != null) {
        if (horarioDisponible(horario)) {
            if (cumpleAnticipacion(horasAnticipacion)) {
                // lógica real — 7 líneas aquí
            }
        }
    }
}
```

`reprogramarReserva()` presentaba el mismo patrón con 4 niveles. `cancelarReserva()` tenía 2 niveles y `confirmarReserva()` tenía 1 nivel innecesario. En todos los casos, los comentarios internos aparecían para compensar la falta de estructura clara.

### Antes (`9aeb870`) — fragmentos con anidamiento

```java
// crearReserva() — 4 niveles:
if (estudiante != null) {
    if (horario != null) {
        if (horarioDisponible(horario)) {
            if (cumpleAnticipacion(horasAnticipacion)) {
                horario.reservar();
                // ...
            }
        }
    }
}

// reprogramarReserva() — 4 niveles:
if (reserva != null) {
    if (nuevoHorario != null) {
        if (horarioDisponible(nuevoHorario)) {
            if (cumpleAnticipacion(horasAnticipacion)) {
                reserva.reprogramar(nuevoHorario);
                // ...
            }
        }
    }
}

// cancelarReserva() — 2 niveles:
if (reserva != null) {
    if (puedeCancelar(reserva, horasAnticipacion)) {
        reserva.cancelar();
        // ...
    }
}

// confirmarReserva() — 1 nivel innecesario:
if (reserva != null) {
    reserva.confirmar();
    // ...
}
```

### Cambio aplicado

Técnica **Guard Clauses**: cada condición de precondición se invierte y retorna temprano. La lógica principal queda alineada al margen izquierdo, sin pirámide.

| Método | Antes | Después |
|---|---|---|
| `crearReserva()` | 4 niveles anidados | 4 guard clauses + lógica plana |
| `reprogramarReserva()` | 4 niveles anidados | 4 guard clauses + lógica plana |
| `cancelarReserva()` | 2 niveles anidados | 2 guard clauses + lógica plana |
| `confirmarReserva()` | 1 nivel innecesario | 1 guard clause + lógica plana |

### Después (`b5cebd9`) — fragmentos con guard clauses

```java
// crearReserva() — flujo plano:
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

// reprogramarReserva() — flujo plano:
if (reserva == null)                       return;
if (nuevoHorario == null)                  return;
if (!horarioDisponible(nuevoHorario))      return;
if (!cumpleAnticipacion(horasAnticipacion)) return;

reserva.reprogramar(nuevoHorario);
repositorio.guardar(reserva);
// ...

// cancelarReserva() — flujo plano:
if (reserva == null)                         return;
if (!puedeCancelar(reserva, horasAnticipacion)) return;

reserva.cancelar();
// ...

// confirmarReserva() — flujo plano:
if (reserva == null) return;

reserva.confirmar();
// ...
```

### Qué mejoró / qué NO cambió

- **Qué mejoró:** el flujo principal de cada método es inmediatamente visible sin indentación. Los comentarios que explicaban qué condición se evaluaba desaparecieron porque la estructura habla por sí misma. La carga cognitiva para leer cada método se reduce de manera proporcional al número de niveles eliminados.
- **Qué NO cambió:** los mismos casos que antes fallaban (estudiante nulo, horario ocupado, anticipación insuficiente) siguen retornando sin modificar el estado. El comportamiento observable es idéntico.

### Verificación

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

### Commit

```
b5cebd9  refactor: reemplazar condicionales anidados con guard clauses en todos los metodos
```

---


## 10. Refactorización 5 — Mixed Responsibilities → Extract Class

### El problema en términos simples

Imagina un cocinero que, además de cocinar, también atiende el teléfono y lleva la contabilidad del restaurante. Cuando el cliente quiere cambiar la forma de pago, hay que hablar con el cocinero. Cuando se cambia el sistema de reservas, hay que hablar con el cocinero. Eso es exactamente lo que pasaba con `ServicioReservas`.

La clase tenía **tres responsabilidades distintas mezcladas en los mismos métodos**:

1. **Lógica de negocio** — validar datos, marcar horarios como ocupados, crear la reserva, registrarla en el estudiante.
2. **Notificaciones** — enviar emails al estudiante cuando algo cambiaba (`System.out.println("EMAIL …")`).
3. **Auditoría y reportes** — imprimir el ticket de confirmación, registrar eventos en el log (`System.out.println("AUDIT: …")`), mostrar el resumen del estudiante.

Esto significaba que un cambio en el formato del email obligaba a tocar el mismo método que contiene la lógica de negocio. Una clase con muchas responsabilidades tiene muchas razones para cambiar, y cada cambio puede romper algo de las otras responsabilidades.

### Antes (`b5cebd9`) — todas las responsabilidades mezcladas

```java
// En ServicioReservas — responsabilidades mezcladas:
public Reserva crearReserva(...) {
    // negocio:
    horario.reservar();
    Reserva reserva = new Reserva(...);
    repositorio.guardar(reserva);
    estudiante.registrarReserva(reserva);

    // notificación — aquí no debería estar:
    System.out.println("EMAIL a " + estudiante.getEmail() + "...");

    // ticket y auditoría — tampoco deberían estar aquí:
    System.out.println("=== TICKET ===");
    System.out.println("AUDIT: reserva ...");

    return reserva;
}

// Y en cancelarReserva(), reprogramarReserva(), confirmarReserva()
// se repetía el mismo patrón: lógica + email + audit mezclados.
```

### La solución: Extract Class

Se crearon dos clases nuevas, cada una con una única responsabilidad:

| Clase nueva | Responsabilidad |
|---|---|
| `NotificadorReservas` | Enviar notificaciones al estudiante (creación, cancelación, reprogramación, confirmación) |
| `AuditorReservas` | Imprimir ticket, registrar en el log de auditoría y mostrar resumen del estudiante |

`ServicioReservas` dejó de hacer esas operaciones directamente y en su lugar llama a los nuevos colaboradores. El constructor original `new ServicioReservas(repositorio)` sigue funcionando exactamente igual — crea internamente un `NotificadorReservas` y un `AuditorReservas` con comportamiento predeterminado.

### Después (`ce727c8`) — cada clase con una sola responsabilidad

**`NotificadorReservas.java`** — sólo notificaciones:
```java
public class NotificadorReservas {
    public void notificarCreacion(Reserva reserva) {
        System.out.println("EMAIL a " + reserva.getEstudiante().getEmail()
                + ": Reserva creada. ID=" + reserva.getId());
    }
    public void notificarCancelacion(Reserva reserva) { ... }
    public void notificarReprogramacion(Reserva reserva) { ... }
    public void notificarConfirmacion(Reserva reserva) { ... }
}
```

**`AuditorReservas.java`** — sólo auditoría y reportes:
```java
public class AuditorReservas {
    public void registrarCreacion(Reserva reserva) {
        System.out.println("=== TICKET ===");
        // ... imprime el ticket ...
        System.out.println("AUDIT: reserva " + reserva.getId() + " creada ...");
    }
    public void registrarCancelacion(Reserva reserva) { ... }
    public void registrarReprogramacion(Reserva reserva) { ... }
    public void registrarConfirmacion(Reserva reserva) { ... }
    public void imprimirResumen(Estudiante estudiante) { ... }
}
```

**`ServicioReservas.java`** — sólo lógica de negocio:
```java
public class ServicioReservas {
    // ...
    public Reserva crearReserva(Estudiante estudiante, HorarioTutoria horario, int horasAnticipacion) {
        if (estudiante == null)                     return null;
        if (horario == null)                        return null;
        if (!horarioDisponible(horario))            return null;
        if (!cumpleAnticipacion(horasAnticipacion)) return null;

        horario.reservar();
        Reserva reserva = new Reserva(contadorId++, estudiante, horario);
        repositorio.guardar(reserva);
        estudiante.registrarReserva(reserva);
        notificador.notificarCreacion(reserva);   // delega
        auditor.registrarCreacion(reserva);        // delega
        return reserva;
    }
    // cancelarReserva, reprogramarReserva, confirmarReserva:
    // misma estructura — sólo lógica de negocio + delegación
}
```

### Qué mejoró / qué NO cambió

- **Qué mejoró:** `ServicioReservas` ahora tiene una sola razón para cambiar — la lógica de negocio. Si se cambia el formato del email, sólo se toca `NotificadorReservas`. Si se cambia el formato del ticket, sólo se toca `AuditorReservas`. Las tres clases pueden entenderse y modificarse de forma independiente.
- **Qué NO cambió:** el comportamiento observable es exactamente el mismo. Las mismas notificaciones se envían en los mismos momentos. Los mismos tickets se imprimen. Los mismos logs se registran. La prueba no se modificó en ninguna línea.

### Verificación

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

### Commit

```
ce727c8  refactor: extraer NotificadorReservas y AuditorReservas para corregir Mixed Responsibilities
```

---


## 11. Refactorización 6 — Comments as Deodorant → Remove Comment

### El problema en términos simples

Un comentario decorativo es como poner una etiqueta que dice "esto es una puerta" en una puerta. No añade ninguna información nueva; sólo ocupa espacio visual y puede convertirse en mentira cuando alguien cambia el código pero olvida actualizar el comentario.

Después de las cinco refactorizaciones anteriores, el código de los smells funcionales ya estaba resuelto, pero las clases de dominio todavía conservaban nueve comentarios que describían exactamente lo que el nombre ya decía:

```java
// En Reserva.java:
// fecha en que se creo la reserva
private final LocalDateTime fechaCreacion;      // ← el nombre lo dice todo

// confirma la reserva si esta pendiente
public void confirmar() { ... }                 // ← el nombre lo dice todo

// cancela la reserva y libera el horario
public void cancelar() { ... }                  // ← el nombre lo dice todo

// reprograma a otro horario
public void reprogramar(HorarioTutoria nuevoHorario) { ... }

// marca como realizada cuando la tutoria ocurrio
public void marcarRealizada() { ... }

// En HorarioTutoria.java:
// true si nadie lo ha reservado todavia
private boolean disponible;                     // ← el nombre + tipo lo dicen todo

// marca como ocupado
public void reservar() { ... }

// libera el horario cuando se cancela
public void liberar() { ... }

// En Estudiante.java:
// devuelve todas las reservas, sin proteger la lista
public List<Reserva> getReservas() { ... }
```

Estos comentarios existían en el código original porque los nombres eran crípticos (`conf()`, `p()`, `disp`). Después de la refactorización 1 (Rename), los nombres se volvieron expresivos y los comentarios perdieron completamente su razón de existir. Se quedaron como residuo que no se limpió en su momento.

### La solución: Remove Comment

La técnica es la más simple de todas las refactorizaciones: **eliminar el comentario**. No hay código nuevo, no hay código movido, no hay renombrado. sólo se quitan las líneas que no aportan información.

| Archivo | Comentarios eliminados |
|---|---|
| `Reserva.java` | 5 comentarios (`fechaCreacion`, `confirmar`, `cancelar`, `reprogramar`, `marcarRealizada`) |
| `HorarioTutoria.java` | 3 comentarios (`disponible`, `reservar`, `liberar`) |
| `Estudiante.java` | 1 comentario (`getReservas`) |

### Antes (`ce727c8`) — fragmento con comentarios decorativos

```java
// Reserva.java — antes:
// fecha en que se creo la reserva
private final LocalDateTime fechaCreacion;

// confirma la reserva si esta pendiente
public void confirmar() {
    if (estado != EstadoReserva.PENDIENTE) { ... }
    this.estado = EstadoReserva.CONFIRMADA;
}

// cancela la reserva y libera el horario
public void cancelar() { ... }
```

### Después (`862f429`) — sin comentarios decorativos

```java
// Reserva.java — después:
private final LocalDateTime fechaCreacion;

public void confirmar() {
    if (estado != EstadoReserva.PENDIENTE) { ... }
    this.estado = EstadoReserva.CONFIRMADA;
}

public void cancelar() { ... }
```

El código dice exactamente lo mismo. La diferencia es que ahora no hay texto que pueda desactualizarse y mentir.

### Qué mejoró / qué NO cambió

- **Qué mejoró:** el código es más limpio visualmente. No hay ruido entre métodos. Un lector nuevo sabe que si hay un comentario en este proyecto, es porque comunica algo que el nombre sólo no puede. La confianza en los comentarios aumenta cuando hay pocos y todos son útiles.
- **Qué NO cambió:** el comportamiento es idéntico. Los comentarios no generaban ni bloqueaban ninguna lógica. Eliminarlos no afecta en nada la ejecución del programa.

### Verificación

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

### Commit

```
862f429  refactor: eliminar comentarios decorativos para corregir Comments as Deodorant
```

---

## 12. Evidencia de comportamiento ANTES / DESPUÉS

| Caso | Base (`c545037`) | R1 (`6192265`) | R2 (`b8c7689`) | R3 (`9aeb870`) | R4 (`b5cebd9`) | R5 (`ce727c8`) | R6 (`862f429`) | ¿Preservado? |
|---|---|---|---|---|---|---|---|---|
| 1 — Crear reserva válida | `PENDIENTE`, horario ocupado | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 2 — Crear con anticipación < 2 h | `null`, horario libre | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 3 — Crear con estudiante `null` | `null`, horario intacto | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 4 — Confirmar reserva | Estado `CONFIRMADA` | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 5 — Cancelar con anticipación suficiente | `CANCELADA`, horario liberado | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 6 — Cancelar con anticipación < 2 h | Estado no cambia | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 7 — Reprogramar a horario disponible | Anterior libre, nuevo ocupado | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 8 — Reprogramar a horario no disponible | Nada cambia | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 9 — `puedeCancelar` válido | `true` | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 10 — `puedeCancelar` nulo | `false` | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |
| 11 — Historial del estudiante | 1 reserva registrada | Igual | Igual | Igual | Igual | Igual | Igual | ✅ Sí |

---

## 13. Comparación técnica antes / después

| Dimensión | Base (`c545037`) | R1 (`6192265`) | R2 (`b8c7689`) | R3 (`9aeb870`) | R4 (`b5cebd9`) | R5 (`ce727c8`) | R6 (`862f429`) | Evidencia |
|---|---|---|---|---|---|---|---|---|
| **Nombres** | `U`, `Est`, `SR`, `proc()` | Nombres de dominio expresivos | Sin cambio | Sin cambio | Sin cambio | Sin cambio | Sin cambio | Rename en todos los archivos |
| **Método principal** | `proc()` — 45 líneas, 8 responsabilidades | `crearReserva()` — nombres legibles | 12 líneas, delega en 3 métodos | Reglas en métodos/constante | Guard clauses, 0 anidamiento | sólo lógica de negocio, delega en 2 clases | Sin cambio | diff commits |
| **Condicionales / flujo** | 4 niveles anidados | Igual | Igual | Igual | Guard clauses en 4 métodos | Sin cambio | Sin cambio | diff `9aeb870..b5cebd9` |
| **Duplicación** | `estaDisponible()` y `2` en 3 lugares | Igual | Igual | `horarioDisponible()`, `cumpleAnticipacion()`, `HORAS_MINIMAS_ANTICIPACION` | Sin cambio | Sin cambio | Sin cambio | diff `b8c7689..9aeb870` |
| **Responsabilidades** | Negocio + persistencia + email + ticket + audit en 1 clase | Igual | Igual | Igual | Igual | 3 clases separadas | Sin cambio | diff `b5cebd9..ce727c8` |
| **Comentarios** | 9 comentarios decorativos en domain | Igual | Igual | Igual | Igual | Igual | 0 comentarios decorativos | diff `ce727c8..862f429` |
| **Comportamiento** | 11 tests verde | 11 tests verde | 11 tests verde | 11 tests verde | 11 tests verde | 11 tests verde | 11 tests verde | `mvn test` → `BUILD SUCCESS` |
| **Git** | `c545037` | `6192265` | `b8c7689` | `9aeb870` | `b5cebd9` | `ce727c8` | `862f429` | `git log --oneline` |

---

## 14. Historial Git

```
862f429 — refactor: eliminar comentarios decorativos para corregir Comments as Deodorant  
ce727c8 — refactor: extraer NotificadorReservas y AuditorReservas para corregir Mixed Responsibilities  
b5cebd9 — refactor: reemplazar condicionales anidados con guard clauses en todos los metodos  
9aeb870 — refactor: extraer horarioDisponible y cumpleAnticipacion para eliminar codigo duplicado  
b8c7689 — refactor: extraer metodos notificarCreacion, imprimirTicket y registrarAuditoria de crearReserva  
6192265 — refactor: corregir Poor Naming en todo el proyecto
c545037 — Initial: registrar linea base con code smells intencionales
```
---

## 15. Código final relevante

Las clases del paquete `service` no cambiaron en esta refactorización. El cambio se realizó en las clases de dominio. Versión final: `862f429`.

### `ServicioReservas.java` — sólo lógica de negocio

```java
package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Docente;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioTutoria;
import edu.uees.tutorias.domain.Reserva;

import java.util.ArrayList;
import java.util.List;

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
```

### `NotificadorReservas.java` — sólo notificaciones

```java
package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Reserva;

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
```

### `AuditorReservas.java` — sólo auditoría y reportes

```java
package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.Reserva;

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
```

### `Reserva.java` — sin comentarios decorativos (`862f429`)

```java
package edu.uees.tutorias.domain;

import java.time.LocalDateTime;

public class Reserva {

    private final Long id;
    private final Estudiante estudiante;
    private HorarioTutoria horario;
    private EstadoReserva estado;
    private final LocalDateTime fechaCreacion;

    public Reserva(Long id, Estudiante estudiante, HorarioTutoria horario) {
        this.id = id;
        this.estudiante = estudiante;
        this.horario = horario;
        this.estado = EstadoReserva.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void confirmar() {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException(
                "sólo se puede confirmar una reserva PENDIENTE. Estado: " + estado);
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.REALIZADA) {
            throw new IllegalStateException("No se puede cancelar. Estado: " + estado);
        }
        this.estado = EstadoReserva.CANCELADA;
        this.horario.liberar();
    }

    public void reprogramar(HorarioTutoria nuevoHorario) {
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.REALIZADA) {
            throw new IllegalStateException("No se puede reprogramar. Estado: " + estado);
        }
        this.horario.liberar();
        nuevoHorario.reservar();
        this.horario = nuevoHorario;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public void marcarRealizada() {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                "sólo se puede marcar realizada una reserva CONFIRMADA. Estado: " + estado);
        }
        this.estado = EstadoReserva.REALIZADA;
    }

    public Long getId()                  { return id; }
    public Estudiante getEstudiante()    { return estudiante; }
    public HorarioTutoria getHorario()   { return horario; }
    public EstadoReserva getEstado()     { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public boolean isCancelada() { return estado == EstadoReserva.CANCELADA; }

    @Override
    public String toString() {
        return "Reserva[id=" + id + ", estado=" + estado
                + ", estudiante=" + estudiante.getNombre()
                + ", horario=" + horario.getId() + "]";
    }
}
```

### `HorarioTutoria.java` — sin comentarios decorativos (`862f429`)

```java
package edu.uees.tutorias.domain;

import java.time.LocalDateTime;

public class HorarioTutoria {

    private final Long id;
    private final LocalDateTime inicio;
    private final LocalDateTime fin;
    private final Asignatura asignatura;
    private boolean disponible;

    public HorarioTutoria(Long id, LocalDateTime inicio, LocalDateTime fin, Asignatura asignatura) {
        this.id = id;
        this.inicio = inicio;
        this.fin = fin;
        this.asignatura = asignatura;
        this.disponible = true;
    }

    public void reservar() {
        if (!disponible) {
            throw new IllegalStateException("Horario ya ocupado.");
        }
        this.disponible = false;
    }

    public void liberar() {
        this.disponible = true;
    }

    public boolean estaDisponible()    { return disponible; }
    public Long getId()                { return id; }
    public LocalDateTime getInicio()   { return inicio; }
    public LocalDateTime getFin()      { return fin; }
    public Asignatura getAsignatura()  { return asignatura; }
}
```

---

## 16. Evidencia de compilación y pruebas

**Tras refactorización 6 (`862f429`):**
```
$ JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn test

[INFO] Building semana5-code-smells 1.0-SNAPSHOT
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time: 6.228 s
[INFO] Finished at: 2026-09-15T21:35:14-05:00
```

Los mismos 11 casos que pasan en la línea base `c545037` pasan en `862f429` sin ninguna modificación a las pruebas.

---

## 17. Conclusiones

El código inicial cumplía su función, pero presentaba seis Code Smells. Se aplicaron seis refactorizaciones de manera incremental:

1. **Rename** (`6192265`): todos los nombres de clases, métodos, campos y parámetros expresan ahora el dominio de tutorías. El cambio fue puramente léxico y no alteró ninguna lógica.

2. **Extract Method** (`b8c7689`): `crearReserva()` bajó de ~45 líneas a 12 al extraer `notificarCreacion`, `imprimirTicket` y `registrarAuditoria`. Cada paso del flujo tiene nombre propio.

3. **Extract Method + Constante** (`9aeb870`): la regla de disponibilidad y el umbral de 2 horas, copiados en tres lugares, se centralizaron en `horarioDisponible()`, `cumpleAnticipacion()` y la constante `HORAS_MINIMAS_ANTICIPACION`.

4. **Guard Clauses** (`b5cebd9`): los 4 niveles de condicionales anidados en `crearReserva()` y `reprogramarReserva()`, y los anidamientos menores en `cancelarReserva()` y `confirmarReserva()`, se reemplazaron por retornos tempranos. La lógica principal de cada método queda alineada al margen, sin pirámide de indentación.

5. **Extract Class** (`ce727c8`): las operaciones de notificación y auditoría se extrajeron de `ServicioReservas` hacia dos clases independientes — `NotificadorReservas` y `AuditorReservas`. Cada clase tiene ahora una única razón para cambiar. El servicio principal se redujo a lógica de negocio pura.

6. **Remove Comment** (`862f429`): se eliminaron los 9 comentarios decorativos que restaban en las clases de dominio (`Reserva`, `HorarioTutoria`, `Estudiante`). Todos repetían lo que el nombre del método o del campo ya comunicaba por sí sólo. El código quedó sin ningún comentario compensatorio.

En cada refactorización se ejecutaron los 11 casos de prueba unitaria, obteniendo los mismos resultados observables que en la línea base. El historial Git evidencia la evolución paso a paso: cada commit es pequeño, tiene un propósito único y puede revisarse de forma independiente.

Los seis Code Smells identificados en la línea base han sido corregidos en su totalidad.

---

## 18. Declaración de uso de IA

Este informe y el proceso de refactorización fueron desarrollados con asistencia de IBM Bob (asistente de IA). El diagnóstico de smells, la selección de técnicas, la verificación de compilación y la escritura del código final fueron guiados y validados por el estudiante. El uso de IA se declara en cumplimiento de las políticas académicas de UEES.

---

## 19. Enlace al repositorio

**Repositorio:** [https://github.com/ismfuees/semana5-code-smells](https://github.com/ismfuees/semana5-code-smells)

Rama: `main`  
`c545037` — Initial: registrar linea base con code smells intencionales  
`6192265` — refactor: corregir Poor Naming en todo el proyecto  
`b8c7689` — refactor: extraer metodos notificarCreacion, imprimirTicket y registrarAuditoria de crearReserva  
`9aeb870` — refactor: extraer horarioDisponible y cumpleAnticipacion para eliminar codigo duplicado  
`b5cebd9` — refactor: reemplazar condicionales anidados con guard clauses en todos los metodos  
`ce727c8` — refactor: extraer NotificadorReservas y AuditorReservas para corregir Mixed Responsibilities  
`862f429` — refactor: eliminar comentarios decorativos para corregir Comments as Deodorant  
 




