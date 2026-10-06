# Selenium2026

![Java](https://img.shields.io/badge/Java-11-orange)
![Selenium](https://img.shields.io/badge/Selenium-4.49.0-43B02A)
![Cucumber](https://img.shields.io/badge/Cucumber-7.34.6-23D96C)
![JUnit](https://img.shields.io/badge/JUnit%20Platform-5.13.4-25A162)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36)

Framework de automatización de pruebas UI con **Selenium WebDriver**, **Cucumber (BDD)** y **JUnit 5 Platform**, con reportes HTML de **ExtentReports**, siguiendo el patrón **Page Object Model**.

Las pruebas se ejecutan contra el [Laboratorio de Prácticas de Novus Technology](https://novustechnology.pe/laboratorio) (formulario, carga de documentos y carrito de compras) y contra [Kiwi.com](https://www.kiwi.com/es).

## Tabla de contenidos

- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Escenarios y tags](#escenarios-y-tags)
- [Reportes](#reportes)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Arquitectura y convenciones](#arquitectura-y-convenciones)
- [Agentes de Claude Code](#agentes-de-claude-code)
- [Autor](#autor)

## Tecnologías

| Herramienta | Versión | Uso |
|---|---|---|
| Java | 11 | Lenguaje |
| Maven | 3.9+ | Build y dependencias |
| Selenium Java | 4.49.0 | Automatización del navegador |
| Cucumber (java + junit-platform-engine) | 7.34.6 | BDD / Gherkin |
| JUnit (BOM) | 5.13.4 | Plataforma de ejecución |
| ExtentReports Cucumber7 Adapter | 1.14.0 | Reporte HTML |
| Apache Commons CSV | 1.14.1 | Lectura de datos de prueba |

## Requisitos previos

- **JDK 11** y **Maven 3.9+** instalados y disponibles en el `PATH`.
- **Google Chrome** o **Mozilla Firefox**. No hace falta descargar el driver: Selenium Manager lo resuelve automáticamente.
- (Opcional) **IntelliJ IDEA** con el plugin **Cucumber for Java** para ejecutar los `.feature` desde el IDE.

Verificar la instalación:

```bash
java -version
mvn -version
```

## Instalación

1. Clonar o descargar el proyecto.
2. Desde la raíz del proyecto, descargar las dependencias y compilar:

   ```bash
   mvn test-compile
   ```

3. En IntelliJ IDEA: **File → Open →** seleccionar `pom.xml` **→ Open as Project**.

## Configuración

### Navegador y URLs

`src/test/resources/config.properties` es el único lugar donde se configuran el navegador y las URLs:

```properties
browser=chrome          # chrome | firefox
url1=https://novustechnology.pe/laboratorio/formulario
url2=https://www.kiwi.com/es
url3=https://novustechnology.pe/laboratorio/carrito-de-compras
url4=https://novustechnology.pe/laboratorio/carga-documentos
```

Si falta una clave, la ejecución falla con `No existe la propiedad '<clave>' en config.properties`.

### Datos de prueba

Ubicados en `src/test/resources/data/`:

| Archivo | Usado en | Detalle |
|---|---|---|
| `test.csv` | `@Csv`, `@CargaMultiple` | Se lee en UTF-8; cada fila se envía y valida por separado. |
| `cucumber.png` | `@CargaDocumentos`, `@CargaMultiple` | Se resuelve desde el classpath (no depende del directorio de ejecución). |

Formato de `test.csv`:

```csv
NombreCompleto,Correo,Pais,Género
Sofia Correa,sofia@test.com,Colombia,Femenino
```

`Pais` y `Género` deben coincidir con las opciones de la página (`Perú`, `Colombia`, `México`, … / `Masculino`, `Femenino`, `Otro`).

## Ejecución

### Desde Maven

```bash
# Todos los escenarios
mvn clean test

# Un feature o escenario por tag
mvn test -Dcucumber.filter.tags="@Carrito"

# Expresiones de tags (and / or / not)
mvn test -Dcucumber.filter.tags="@Novus and not @Csv"
```

> [!IMPORTANT]
> Estos comandos solo funcionan si `RunCucumberTest` **no** tiene un filtro de tags fijo (ver abajo). La anotación del runner tiene prioridad sobre `-Dcucumber.filter.tags`, que sería ignorado.

### Desde IntelliJ IDEA

- **Un feature:** clic derecho sobre el `.feature` → **Run** (requiere el plugin Cucumber for Java).
- **Toda la suite:** clic derecho sobre `RunCucumberTest` → **Run**.
- **Un tag desde el runner:** agregar temporalmente en `RunCucumberTest`:

  ```java
  @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Kiwi")
  ```

  > [!WARNING]
  > Este filtro aplica a **todas** las ejecuciones, incluida `mvn clean test`. Comentarlo o eliminarlo al terminar.

  Alternativa sin modificar la clase: en **Run → Edit Configurations… → VM options** de `RunCucumberTest`, agregar `-Dcucumber.filter.tags="@Kiwi"`.

## Escenarios y tags

| Feature | Tags | Escenario | Sitio |
|---|---|---|---|
| `Formulario.feature` | `@Novus` `@Formulario` | Llenado del formulario (DataTable + Scenario Outline) y validación de cada campo en el modal de resumen | Novus – Formulario |
| | `@Novus` `@Error` | Validación HTML5 de campo obligatorio (Nombre completo) | |
| | `@Novus` `@Toast` | Toast "Notificación de práctica" visible y con el texto correcto | |
| | `@Novus` `@Alerta` | Alerta JavaScript (`window.alert`): valida su texto y la acepta | |
| | `@Novus` `@Csv` | Ingreso de cada registro de `test.csv` validando su resumen | |
| `CargaDocumentos.feature` | `@Novus` `@CargaDocumentos` | Subida de imagen, validación en la lista de archivos y mensaje de éxito | Novus – Carga de documentos |
| | `@Novus` `@CargaMultiple` | Subida de varios archivos a la vez (`#archivos-multiples`), validación de todos en la lista y mensaje de éxito | |
| `Carrito.feature` | `@Carrito` `@Carrito1` | Compra de una cantidad fija con tarjeta generada | Novus – Carrito de compras (réplica de Guru99) |
| | `@Carrito` `@Carrito2` | Compra de una cantidad parametrizada, validando "Payment successfull!" y el Order ID | |
| `Kiwi.feature` | `@Kiwi` | Selección de origen en el buscador de vuelos y validación del origen elegido | Kiwi.com |

## Reportes

Al finalizar cada ejecución, ExtentReports genera una carpeta en la raíz del proyecto:

```
test-reports <d-MMM-YY HH-mm-ss>/test-output/htmlreports/automation-test-report.html
```

- Si un paso falla, se adjunta un screenshot al reporte.
- La información del sistema (SO, versión, Java y navegador) se toma de la máquina donde se ejecuta.
- Las carpetas `test-reports*` están excluidas en `.gitignore`.

## Estructura del proyecto

```
src/test/
├── java/
│   ├── RunCucumberTest.java        # Runner de Cucumber (JUnit Platform Suite)
│   ├── base/
│   │   ├── BasePage.java           # Clase padre de los Page Objects (driver + WebDriverWait 30s)
│   │   ├── ConfigFileReader.java   # Lectura estática de config.properties (se carga una sola vez)
│   │   └── CardContext.java        # Datos de tarjeta compartidos entre páginas del escenario
│   ├── page/                       # Page Objects (@FindBy + PageFactory)
│   │   ├── FormularioPage.java     # Formulario de práctica + lectura del CSV
│   │   ├── ToastAlertPage.java     # Toast y alerta del formulario
│   │   ├── CargaDocumentosPage.java
│   │   ├── HomePage.java           # Carrito: producto, cantidad y Buy Now
│   │   ├── DatosTarjetaPage.java   # Carrito: captura la tarjeta generada en otra pestaña
│   │   ├── DatosPagoPage.java      # Carrito: ingresa la tarjeta y paga
│   │   ├── ValidarPagoPage.java    # Carrito: mensaje de pago y Order ID
│   │   └── KiwiPage.java
│   └── step/
│       ├── Hooks.java              # Crea/cierra el WebDriver, screenshots e info del sistema en el reporte
│       └── *Step.java              # Step definitions
└── resources/
    ├── features/                   # Escenarios Gherkin (en español)
    ├── data/                       # Datos de prueba
    ├── config.properties           # Navegador y URLs
    ├── cucumber.properties         # Configuración de Cucumber
    ├── extent.properties           # Configuración del reporte
    └── spark-config.xml            # Tema del reporte Spark
```

## Arquitectura y convenciones

### Flujo de ejecución

`RunCucumberTest` → descubre los `.feature` de `features/` → `Hooks` abre el navegador (`@Before`) → cada paso Gherkin ejecuta un método de `step/*Step.java` → que usa los Page Objects de `page/` → `Hooks` cierra el navegador (`@After`).

### Agregar un escenario nuevo

1. **Feature:** escribir el escenario en `src/test/resources/features/` con su tag.
2. **Page Object:** crear la clase en `page/` extendiendo `BasePage`, con los locators como `@FindBy` y llamando a `PageFactory.initElements(driver, this)` en el constructor.
3. **Steps:** crear la clase en `step/` que instancie el Page Object con `Hooks.getDriver()` y contenga los asserts (`org.junit.jupiter.api.Assertions`).
4. **URL:** si es una página nueva, agregarla en `config.properties` y leerla con `ConfigFileReader.getProp("urlN")`.

### Convenciones

- **Idioma:** features, steps, métodos y comentarios en español.
- **Esperas:** solo explícitas con `wait.until(ExpectedConditions...)` antes de interactuar. **No usar espera implícita** (está desactivada a propósito, ver `Hooks`).
- **Locators:** preferir `id` o `data-testid`; evitar XPath posicionales.
- **Datos entre páginas:** usar un objeto de contexto pasado por constructor (como `CardContext`), no variables `static`.
- **Validaciones independientes del idioma:** la validación HTML5 se comprueba con `validity.valueMissing` y en Kiwi los nombres se comparan sin tildes (`Milán` = `Milan`).

## Agentes de Claude Code

El proyecto incluye subagentes de Claude Code (`.claude/agents/`) para inspeccionar locators, diagnosticar fallos y generar escenarios (incluso a partir de un `.feature` escrito por ti). Ver el [Manual de uso de los agentes](docs/MANUAL-AGENTES.md).

## Autor

**[Novus Technology](https://novustechnology.pe)** — Formación en QA, Testing y Automatización.

- Web: [novustechnology.pe](https://novustechnology.pe)
- Laboratorio de prácticas: [novustechnology.pe/laboratorio](https://novustechnology.pe/laboratorio)
- Contacto: [contacto@novustechnology.pe](mailto:contacto@novustechnology.pe)
