# Manual de uso de los agentes

Guía para usar los subagentes de Claude Code incluidos en este proyecto (`.claude/agents/`).

## Contenido

- [Conceptos básicos](#conceptos-básicos)
- [Requisitos](#requisitos)
- [Cómo invocar un agente](#cómo-invocar-un-agente)
- [inspector-locators](#inspector-locators)
- [diagnosticador-fallos](#diagnosticador-fallos)
- [generador-escenarios](#generador-escenarios)
- [Generar el código a partir de tu propio feature](#generar-el-código-a-partir-de-tu-propio-feature)
- [Revisión del Gherkin: declarativo vs imperativo](#revisión-del-gherkin-declarativo-vs-imperativo)
- [Flujos de trabajo recomendados](#flujos-de-trabajo-recomendados)
- [Herramienta InspectorPagina (uso manual)](#herramienta-inspectorpagina-uso-manual)
- [Límites y buenas prácticas](#límites-y-buenas-prácticas)
- [Mantenimiento de los agentes](#mantenimiento-de-los-agentes)

## Conceptos básicos

- **Agente principal:** es la sesión de Claude Code con la que conversas. Siempre existe.
- **Subagente:** un asistente especializado que el agente principal **delega** para una tarea concreta. Trabaja con sus propias instrucciones y herramientas y devuelve un informe al agente principal, que te lo resume.
- Los subagentes están definidos como archivos Markdown en `.claude/agents/` y se versionan con el proyecto.

| Agente | Para qué sirve | ¿Modifica archivos? |
|---|---|---|
| `inspector-locators` | Verificar si los locators siguen existiendo en la web o descubrir los de una página nueva | No |
| `diagnosticador-fallos` | Ejecutar escenarios y explicar por qué fallan | No |
| `generador-escenarios` | Crear o adaptar escenarios completos (feature + steps + Page Object) y revisar la calidad del Gherkin | Sí (no en modo "solo análisis") |

## Requisitos

- Abrir Claude Code **desde la raíz del proyecto**.
- JDK 11, Maven y Google Chrome instalados (los mismos requisitos del [README](../README.md)).
- Conexión a internet (los agentes abren las páginas reales).
- Si se acaban de crear o modificar los archivos de `.claude/agents/`, **reiniciar Claude Code**: los agentes se cargan al iniciar la sesión.
- Comprobar que están disponibles con el comando `/agents`.

## Cómo invocar un agente

No hace falta ningún comando especial: se pide en lenguaje natural y Claude Code elige el agente según la descripción de cada uno.

```text
¿Por qué falla @Carrito2?
```

Para forzar un agente concreto, nombrarlo en el pedido:

```text
Usa el inspector-locators para verificar KiwiPage
```

Mientras un agente trabaja, la sesión muestra su progreso. Al terminar, el agente principal resume el resultado y propone los siguientes pasos.

---

## inspector-locators

**Úsalo cuando:**
- Una prueba falla con `NoSuchElementException` o `TimeoutException` esperando un elemento.
- Sospechas que una página cambió (rediseño, nueva URL).
- Vas a automatizar una página nueva y necesitas sus locators.

**Ejemplos de pedidos:**

```text
Verifica los locators de FormularioPage
¿Qué ids tiene la página de carga de documentos?
Inspecciona el modal que aparece después de enviar el formulario
¿Siguen funcionando los locators del carrito después de "Buy Now"?
```

**Qué hace:**
1. Lee el Page Object y extrae cada locator (`@FindBy` y `By.*`).
2. Abre la página real con la herramienta `InspectorPagina` (Chrome en modo headless).
3. Si el elemento aparece tras un flujo (modal, página de pago, pestaña nueva), reproduce los pasos previos.
4. Compara y propone reemplazos para los locators rotos, priorizando `id` y `data-testid`.

**Qué devuelve:**
- URL y título de la página inspeccionada.
- Tabla `Locator | Clase.campo | Estado | Sugerencia`, con estados `OK`, `NO EXISTE`, `NO VISIBLE` o `REQUIERE FLUJO`.
- Elementos de la página que el Page Object todavía no usa (ideas de cobertura).

**Tiempo aproximado:** 1–2 minutos.

---

## diagnosticador-fallos

**Úsalo cuando:**
- Un escenario o la suite completa falla.
- Una ejecución se comporta de forma rara: corre otro tag, todo sale "skipped" o tarda demasiado.

**Ejemplos de pedidos:**

```text
¿Por qué falla @Carrito2?
Diagnostica la suite completa
@Novus tarda mucho y falla, revisa qué pasa
```

**Qué hace:**
1. Revisa la configuración: que `RunCucumberTest` no tenga un filtro de tags fijo y que existan las claves de `config.properties`.
2. Ejecuta el tag con Maven.
3. Lee `target/surefire-reports/TEST-RunCucumberTest.xml`, los stack traces y el paso exacto del `.feature`.
4. Si el fallo parece de timing, repite la ejecución para confirmar si es intermitente.
5. Clasifica cada fallo:

| Categoría | Señal típica |
|---|---|
| Configuración | Filtro fijo en el runner, clave faltante, navegador no soportado |
| Sitio caído / URL cambiada | 404, redirección, `NoSuchWindowException` en todo un sitio |
| Locator roto | `NoSuchElementException`, timeout esperando un elemento |
| Timing / carrera | Lee la página anterior, ventana aún no abierta, falta una espera explícita |
| Overlay / popup | `ElementClickInterceptedException` |
| Dato o texto esperado | `AssertionFailedError` con expected ≠ actual |
| Dato inválido para un locator | `InvalidSelectorException` por comilla simple en un dato |

**Qué devuelve:** resumen de la ejecución y, por cada fallo, la categoría, la evidencia (`archivo:línea`), la causa raíz y el **arreglo propuesto con el antes y el después**. No aplica el arreglo: tú decides y se lo pides al agente principal.

**Tiempo aproximado:** 1–3 minutos por tag (más si repite ejecuciones).

---

## generador-escenarios

**Úsalo cuando:**
- Quieres cubrir una funcionalidad nueva.
- Una página cambió y hay que adaptar un feature existente.
- Tienes un `.feature` escrito y quieres que se genere todo el código ([ver sección siguiente](#generar-el-código-a-partir-de-tu-propio-feature)).

**Ejemplos de pedidos:**

```text
Crea un escenario para el "Check Credit Card Limit" del carrito
Agrega un escenario que pruebe el botón "Limpiar formulario"
Automatiza la carga de archivos por arrastrar y soltar
```

**Qué hace:**
1. Lee `CLAUDE.md` (arquitectura, convenciones y decisiones del proyecto).
2. Inspecciona la página real para obtener los locators. Nunca los inventa.
3. Busca qué existe para reutilizarlo: Page Objects, steps, URLs y datos.
4. Escribe o actualiza:
   - el `.feature`;
   - los steps (`src/test/java/step/`);
   - el Page Object (`src/test/java/page/`);
   - la URL en `config.properties`, si es una página nueva.
5. Ejecuta el escenario nuevo, hace una **prueba negativa** (cambia un valor esperado, comprueba que falla y lo revierte) y ejecuta la **suite completa**.
6. Actualiza la tabla de escenarios del `README.md` y la lista de tags de `CLAUDE.md`.

**Convenciones que aplica automáticamente:**
- español;
- solo esperas explícitas;
- locators por `id` o `data-testid`;
- `assertEquals(esperado, actual)` con mensaje;
- `ConfigFileReader.getProp`;
- archivos de datos desde el classpath;
- patrón `CardContext` para compartir datos entre páginas.

**Qué devuelve:** los archivos creados o modificados, el Gherkin final y los resultados de las 3 ejecuciones.

**Tiempo aproximado:** 3–6 minutos.

> [!IMPORTANT]
> Es el único agente que modifica archivos. Revisa lo que genera antes de hacer commit (`git diff`). Nunca hace commits por su cuenta.

---

## Generar el código a partir de tu propio feature

**Sí, se puede.** Escribe (o copia) el escenario en Gherkin y pídele al generador que cree todo el código.

### Paso a paso

1. Pega el feature en el chat, o guárdalo primero en `src/test/resources/features/` y menciona el archivo.
2. Indica **la página** a la que corresponde (URL o nombre), si no es evidente por el texto.
3. Pide la generación:

```text
Usa el generador-escenarios para generar todo el código de este feature
para la página https://novustechnology.pe/laboratorio/carrito-de-compras:

@Novus @LimiteTarjeta
Feature: Límite de la tarjeta de crédito

  Scenario: Consultar el límite de una tarjeta generada
    Given que accedo a la pagina de carrito de compras de NovusTechnology
    When doy click en generar tarjeta
    And capturo los datos de la tarjeta
    And consulto el limite de la tarjeta
    Then valido que el limite disponible sea "$100.00"
```

### Qué hace el agente con tu feature

- **Respeta tu Gherkin:** no cambia textos de pasos, nombres, tags ni datos.
- **Reutiliza los steps existentes** cuando el texto coincide exactamente. En el ejemplo, los tres primeros pasos ya existen en `CarritoStep`, así que solo crea los dos últimos.
- Si un paso es **casi igual** a uno existente (por ejemplo "accedo a la página DemoGuru" frente a "que accedo a la pagina de carrito de compras de NovusTechnology"), **no crea un duplicado**: te avisa y sugiere usar el texto existente. Así se evita el error `DuplicateStepDefinitionException` de Cucumber.
- Inspecciona la página para cada locator nuevo.
- Si un paso no se puede automatizar como está escrito (no dice qué validar o el elemento no existe), **te pregunta** antes de cambiarlo.
- Al final te da la lista de pasos reutilizados, pasos nuevos, métodos nuevos y pasos que tuvo que interpretar.
- Además incluye una **revisión del Gherkin** con propuestas de mejora (ver sección siguiente). El código se genera para tu feature tal como lo escribiste; las mejoras quedan como propuesta.

### Consejos para escribir un feature que se genere bien

- **Un paso = una acción o una validación**, escrito como lo haría un usuario.
- Pon **datos concretos entre comillas** (`"Milan"`, `"$100.00"`); se convierten en parámetros `{string}` reutilizables.
- **Termina con un `Then` que valide algo visible:** un texto, un valor o la presencia de un elemento. Por ejemplo: "valido que el limite disponible sea "$100.00"".
- **Reutiliza la redacción de los pasos existentes.** Para verlos, pídele al agente principal "lista los steps existentes" o revisa los `.feature` actuales.
- Agrega un **tag propio** por escenario y el **tag del sitio** (`@Novus`, `@Kiwi`).
- Para varios registros con varios campos usa un `DataTable` o `Scenario Outline` (ver `Formulario.feature`). Para pocos valores simples, un string separado por comas (ver `@CargaMultiple`).

### Qué no puede hacer

- Automatizar algo que la página no permite: CAPTCHAs, login real o pagos reales.
- Garantizar estabilidad en sitios de terceros muy dinámicos (por ejemplo Kiwi): puede requerir ajustes manuales.
- Inventar el resultado esperado si el feature no lo indica: te lo preguntará.

---

## Revisión del Gherkin: declarativo vs imperativo

El generador evalúa la calidad del Gherkin con un enfoque de automatización y propone mejoras. Lo hace siempre que genera código a partir de tu feature, y también se le puede pedir **solo el análisis** (en ese caso no crea ni modifica archivos ni ejecuta pruebas):

```text
Revisa Carrito.feature: ¿debería ser declarativo o imperativo? Solo analiza
Analiza este feature antes de generar el código: <pegar Gherkin>
```

### Declarativo vs imperativo

| | Declarativo | Imperativo |
|---|---|---|
| Describe | **Qué** hace el usuario y el resultado de negocio | **Cómo** se interactúa con la UI |
| Ejemplo | `When compro 3 productos con una tarjeta válida` | `When doy click en generar tarjeta` / `And ingreso los datos de la tarjeta` |
| Ventaja | Sobrevive a cambios de UI, se lee como especificación | Muestra cada acción; fácil de mapear a código |
| Desventaja | Steps más grandes que agrupan varias acciones | Se rompe con cambios de UI, escenarios largos |
| Conviene en este proyecto | Flujos de negocio de varios pasos (`@Carrito`, `@Formulario`) | Pruebas de un componente de UI donde la interacción **es** lo que se prueba (`@Toast`, `@Alerta`, `@Error`) |

### Qué más revisa

- **Un comportamiento por escenario.**
- Uso de `Background` para pasos repetidos y de `Scenario Outline` para el mismo flujo con distintos datos.
- **Cada escenario termina validando algo observable** con un dato concreto.
- Datos en el feature en lugar de valores fijos en el código.
- Redacción alineada con los steps existentes, para no crear duplicados.
- Consistencia: persona y tiempo verbal, ortografía, nombres de escenario únicos y tags.
- Que no haya detalles técnicos en el Gherkin: ids, XPath, URLs o esperas.

### Qué devuelve

1. **Diagnóstico** del estilo actual y si es adecuado para lo que prueba.
2. **Tabla de problemas:** paso o escenario, por qué afecta a la automatización y propuesta.
3. **Versión propuesta del feature**, indicando qué steps se reutilizan y cuáles serían nuevos.
4. **Impacto en el código** si aceptas la propuesta.

Si te convence la propuesta, pide al generador que la aplique, por ejemplo: "aplica la versión propuesta de Carrito.feature".

---

## Flujos de trabajo recomendados

### Una prueba que antes pasaba ahora falla

```text
1. "¿Por qué falla @<Tag>?"                      → diagnosticador-fallos
2. Si la categoría es "Locator roto":
   "Verifica los locators de <Page>"              → inspector-locators
3. "Aplica el arreglo propuesto"                  → agente principal
4. "Ejecuta @<Tag>" para confirmar
```

### La página cambió de diseño

```text
1. "Inspecciona <URL> y compárala con <Page>"    → inspector-locators
2. "Adapta <Feature> a la nueva página"           → generador-escenarios
3. Revisar con git diff
```

### Nueva funcionalidad a cubrir

```text
1. "¿Qué elementos tiene <URL> que no estemos probando?" → inspector-locators
2. Escribir el feature (o pedírselo al generador)
3. "Genera el código de este feature"                     → generador-escenarios
```

---

## Herramienta InspectorPagina (uso manual)

Los agentes usan `.claude/tools/InspectorPagina.java`, pero también puedes ejecutarla tú desde la raíz del proyecto:

```bash
# Una sola vez (o cuando cambien las dependencias del pom.xml)
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/selenium-cp.txt

# Inspeccionar una página
java -cp "$(cat /tmp/selenium-cp.txt)" .claude/tools/InspectorPagina.java <url> --headless [opciones]
```

| Opción | Uso |
|---|---|
| `--click=<sel>` | Click en botones, checkboxes, radios o enlaces |
| `--escribir=<sel>::<texto>` | Escribe en un input o textarea |
| `--seleccionar=<sel>::<opción>` | Elige una opción de un `<select>` (por texto visible o value) |
| `--subir=<sel>::<archivo>[,<archivo>]` | Adjunta archivos a un input file (rutas relativas a la raíz) |
| `--nueva-ventana` | Cambia a la última pestaña abierta |
| `--esperar=<ms>` | Pausa fija |
| `--verificar=<sel>` | Indica si el selector existe, cuántos hay y si es visible |
| `--headless` | Ejecuta Chrome sin interfaz (quitarlo para ver el navegador) |

Los selectores son CSS por defecto; usa el prefijo `xpath=` para XPath. Las opciones se ejecutan **en el orden escrito**.

Ejemplo: llenar el formulario, enviarlo y verificar el modal de resumen:

```bash
java -cp "$(cat /tmp/selenium-cp.txt)" .claude/tools/InspectorPagina.java \
  https://novustechnology.pe/laboratorio/formulario --headless \
  "--escribir=#nombre-completo::York Correa" "--escribir=#correo::york@test.com" \
  "--seleccionar=#pais::Perú" --click=#aceptar-terminos --click=#btn-enviar \
  "--verificar=#modal-resumen-titulo" "--verificar=[data-testid='resumen-nombre']"
```

La salida tiene 4 secciones:
- **ACCIONES:** `OK` o `FALLÓ` por cada paso.
- **PÁGINA:** URL, título y número de ventanas.
- **ELEMENTOS:** ids, `data-testid`, `name`, value, `checked`, opciones y textos.
- **VERIFICACIÓN DE SELECTORES.**

El aviso `Unable to find an exact match for CDP version` es conocido y no afecta.

---

## Límites y buenas prácticas

- **Revisa siempre el código generado** antes de hacer commit. Los agentes no hacen commits.
- **Los agentes respetan las decisiones registradas** en "Pendientes acordados" de `CLAUDE.md`, por ejemplo:
  - XPath concatenados;
  - `@Carrito1` sin validación final;
  - sin parámetros `-D` para el navegador.

  Si cambias de opinión sobre alguna, actualiza primero `CLAUDE.md`.
- **Un pedido por vez y con contexto concreto:** tag, Page Object o URL. "Revisa todo" es más lento y menos preciso.
- Los agentes **abren navegadores reales**: necesitan internet, consumen tiempo y pueden fallar si el sitio está caído.
- Si `RunCucumberTest` tiene un filtro de tags activo (`FILTER_TAGS_PROPERTY_NAME`), Maven ignora `-Dcucumber.filter.tags`. Los agentes lo revisan, pero conviene dejarlo comentado.

## Mantenimiento de los agentes

- Cada agente es un archivo Markdown en `.claude/agents/` con un encabezado (`name`, `description`, `tools`, `model`) y sus instrucciones.
- Para cambiar su comportamiento, edita el archivo y **reinicia Claude Code**.
- La `description` determina **cuándo** Claude Code elige el agente automáticamente: mantenla con ejemplos de pedidos reales.
- Las convenciones generales viven en `CLAUDE.md`; los agentes lo leen en cada uso. Si cambia una convención, basta con actualizar `CLAUDE.md`.
- Para agregar un agente nuevo, crea otro archivo en `.claude/agents/` siguiendo el formato de los existentes, o pídele a Claude Code que lo cree.
