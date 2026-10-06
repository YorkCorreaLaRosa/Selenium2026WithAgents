# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Framework de automatización UI en **Java 11 + Selenium 4 + Cucumber 7 (JUnit Platform)** con reportes **ExtentReports**. El código, los steps y los features están en español; mantener ese idioma.

## Comandos

```bash
mvn clean test                                        # ejecuta todos los features
mvn test -Dcucumber.filter.tags="@Carrito2"           # un escenario/feature por tag
mvn test -Dcucumber.filter.tags="@Novus and not @Csv" # expresiones de tags
mvn test-compile                                      # solo compilar
```

- Surefire solo incluye `RunCucumberTest.java`; no hay tests JUnit sueltos. Filtrar siempre por tag, no por clase.
- Si `RunCucumberTest` tiene `@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, ...)`, ese filtro **tiene prioridad** sobre `-Dcucumber.filter.tags` (verificado): `mvn` ejecutará solo ese tag. Revisar el runner antes de concluir que una ejecución "corrió todo".
- Tags disponibles: `@Carrito` (`@Carrito1`, `@Carrito2`), `@Kiwi`, `@Novus` (`@Formulario`, `@Error`, `@Toast`, `@Alerta`, `@Csv`, `@CargaDocumentos`, `@CargaMultiple`).
- Reporte HTML: se genera en carpetas `test-reports <fecha>/` en la raíz (config en `src/test/resources/extent.properties`, `basefolder.*`). Están en `.gitignore`.

## Arquitectura

Todo vive en `src/test/` (no hay `src/main`).

- **Runner** `java/RunCucumberTest.java`: suite de JUnit Platform con `@SelectPackages("features")` → `resources/features/*.feature`, glue = paquete `step`, plugin `ExtentCucumberAdapter`.
- **Driver** `step/Hooks.java`: crea el `WebDriver` en `@Before` según `browser` de `config.properties` (chrome/firefox), lo guarda en un `ThreadLocal` y se obtiene con `Hooks.getDriver()`. `@AfterStep` adjunta screenshot si falla; `@After` hace `quit()`. No usa espera implícita (a propósito, para no mezclarla con la explícita): toda espera va en los Page Objects con el `WebDriverWait` de 30s de `BasePage`.
- **Steps** `step/*Step.java`: en el constructor instancian los Page Objects pasándoles `Hooks.getDriver()`. Cucumber crea una instancia nueva de cada clase de steps por escenario.
- **Page Objects** `page/*Page.java`: extienden `base.BasePage` (expone `driver` y `wait`) y usan `PageFactory.initElements` con `@FindBy`. Esperar con `wait.until(ExpectedConditions...)` antes de interactuar.
- **Estado compartido entre páginas**: `base.CardContext` (POJO) se crea en `CarritoStep` y se pasa a `DatosTarjetaPage` (captura tarjeta/CVV/fecha en otra ventana) y `DatosPagoPage` (los ingresa). Seguir este patrón para compartir datos entre pages del mismo escenario.
- **Configuración** `ConfigFileReader.getProp(key)` (estático; carga `resources/config.properties` del classpath una sola vez y falla si la clave no existe): `browser`, `url1` (Novus `/laboratorio/formulario`), `url2` (Kiwi), `url3` (Novus `/laboratorio/carrito-de-compras`, réplica de Guru99), `url4` (Novus `/laboratorio/carga-documentos`).
- **Datos de prueba** `resources/data/`: `test.csv` (headers `NombreCompleto,Correo,Pais,Género`; leído en UTF-8 desde classpath por `FormularioPage.leerDatosCsv()`) y `cucumber.png` (resuelto desde classpath en `CargaDocumentosPage`; `@CargaMultiple` sube ambos a la vez).
- **Laboratorio Novus**: la web es una app Next.js con ids/`data-testid` estables (`#nombre-completo`, `[data-testid='resumen-<campo>']`, `#toast-mensaje`, `#btn-alerta`). La validación de campos obligatorios es la nativa de HTML5: su texto depende del idioma del navegador, por eso se valida con `validity.valueMissing` y no con un texto fijo.

## Notas

- El `junit-bom` en `pom.xml` es necesario: alinea versiones de JUnit Platform que trae `cucumber-junit-platform-engine`. No quitarlo.
- Paralelismo: `cucumber.properties` lo tiene en `false` a propósito (el adapter de Extent no está verificado con concurrencia); el driver ya es `ThreadLocal`, listo para activarlo.

## Subagentes (`.claude/agents/`)

Manual de uso: `docs/MANUAL-AGENTES.md` (mantenerlo al día si cambian los agentes o la herramienta).

- `inspector-locators`: verifica locators de un Page Object contra la web real o descubre los de una página nueva (solo lectura).
- `diagnosticador-fallos`: ejecuta un tag, analiza surefire XML y clasifica la causa de cada fallo (solo lectura; propone arreglos).
- `generador-escenarios`: crea/adapta feature + steps + Page Object siguiendo estas convenciones y los ejecuta.
- Herramienta compartida: `.claude/tools/InspectorPagina.java` (Java 11 single-file launch, no forma parte de la suite):
  `mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/selenium-cp.txt && java -cp "$(cat /tmp/selenium-cp.txt)" .claude/tools/InspectorPagina.java <url> --headless [--escribir=sel::texto] [--seleccionar=sel::opcion] [--subir=sel::archivos] [--click=sel] [--nueva-ventana] [--verificar=sel]`

## Pendientes acordados

- `@Carrito1` termina sin validar el pago: **se deja así a propósito** (decisión del usuario).
- `Carrito.feature` es un **feature de ejemplo** y se mantiene en estilo imperativo tal como está: no proponer reescribirlo a declarativo (decisión del usuario).
- **No se implementa** override por línea de comandos (`-Dbrowser` / `-Dheadless`): decisión del usuario. El navegador se configura solo en `config.properties`.
- **Pendiente externo**: actualizar Selenium cuando salga una versión con soporte CDP 154 (a 2026-09-30 la última es 4.49.0, solo hasta CDP 153; el warning no afecta a los tests).
- **Se mantienen** los XPath armados concatenando texto (`KiwiPage`, `FormularioPage`, `CargaDocumentosPage`): decisión del usuario. Si un dato del feature/CSV trae comilla simple (ej. `L'Aquila`), el locator queda mal formado y Selenium lanza `InvalidSelectorException`: esa es la causa, no la página.
