import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Herramienta de inspección para los subagentes de Claude Code (no forma parte de la suite de pruebas).
 *
 * Uso (Java 11 single-file launch, desde la raíz del proyecto):
 *   mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/selenium-cp.txt
 *   java -cp "$(cat /tmp/selenium-cp.txt)" .claude/tools/InspectorPagina.java <url> [opciones]
 *
 * Opciones (se ejecutan en el orden indicado):
 *   --click=<selector>         hace click en el elemento (botones, checkboxes, radios, enlaces)
 *   --escribir=<selector>::<texto>  escribe texto en un input/textarea
 *   --seleccionar=<selector>::<opcion>  elige una opción de un <select> por texto visible (o por value si no hay texto igual)
 *   --subir=<selector>::<archivo>[,<archivo>...]  adjunta archivos a un input type=file (rutas relativas a la raíz del proyecto)
 *   --esperar=<ms>             pausa fija en milisegundos (por defecto 4000 tras cargar la URL)
 *   --nueva-ventana            cambia a la última ventana/pestaña abierta
 *   --verificar=<selector>     informa si el selector existe, cuántos hay y si es visible
 *   --headless                 ejecuta Chrome sin interfaz
 *
 * Selectores: CSS por defecto; prefijo "xpath=" para XPath (ej. xpath=//h1).
 */
public class InspectorPagina {

    private static final String SCRIPT_ELEMENTOS =
            "var raiz = document.querySelector('main') || document.body;" +
            "return [...raiz.querySelectorAll('[id],[data-testid],[data-test],input,select,textarea,button,a,h1,h2,h3,h4')]" +
            ".map(e => {" +
            "  var t = e.tagName.toLowerCase();" +
            "  var partes = [t];" +
            "  if (e.id) partes.push('id=' + e.id);" +
            "  if (e.dataset.testid) partes.push('data-testid=' + e.dataset.testid);" +
            "  if (e.dataset.test) partes.push('data-test=' + e.dataset.test);" +
            "  if (e.name) partes.push('name=' + e.name);" +
            "  if (e.type && t !== 'button' && t !== 'a') partes.push('type=' + e.type);" +
            "  if (e.placeholder) partes.push('placeholder=' + e.placeholder);" +
            "  if (e.value && (t === 'input' || t === 'textarea') && e.type !== 'file') partes.push('value=' + e.value);" +
            "  if (e.type === 'checkbox' || e.type === 'radio') partes.push('checked=' + e.checked);" +
            "  if (t === 'select') partes.push('opciones=[' + [...e.options].map(o => o.value + '=' + o.text.trim()).join(', ') + ']');" +
            "  else { var txt = (e.innerText || e.textContent || '').trim().replace(/\\s+/g, ' ');" +
            "         if (txt && e.children.length < 4) partes.push('texto=\"' + txt.slice(0, 60) + '\"'); }" +
            "  return partes.join(' | ');" +
            "}).filter((v, i, arr) => arr.indexOf(v) === i).join('\\n');";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Uso: InspectorPagina <url> [--click=sel] [--escribir=sel::texto] [--seleccionar=sel::opcion] [--subir=sel::archivos] [--esperar=ms] [--nueva-ventana] [--verificar=sel] [--headless]");
            System.exit(1);
        }

        boolean headless = false;
        for (String arg : args) {
            if (arg.equals("--headless")) {
                headless = true;
            }
        }

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }

        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<String> verificaciones = new ArrayList<>();
        try {
            if (!headless) {
                driver.manage().window().maximize();
            }
            driver.get(args[0]);
            Thread.sleep(4000);

            System.out.println("=== ACCIONES");
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--headless")) {
                    continue;
                }
                if (arg.startsWith("--verificar=")) {
                    verificaciones.add(arg.substring("--verificar=".length()));
                    continue;
                }
                try {
                    if (arg.startsWith("--click=")) {
                        driver.findElement(localizador(arg.substring("--click=".length()))).click();
                        Thread.sleep(1500);
                    } else if (arg.startsWith("--escribir=")) {
                        String[] partes = arg.substring("--escribir=".length()).split("::", 2);
                        driver.findElement(localizador(partes[0])).sendKeys(partes.length > 1 ? partes[1] : "");
                    } else if (arg.startsWith("--seleccionar=")) {
                        String[] partes = arg.substring("--seleccionar=".length()).split("::", 2);
                        Select select = new Select(driver.findElement(localizador(partes[0])));
                        try {
                            select.selectByVisibleText(partes[1]);
                        } catch (Exception porTexto) {
                            select.selectByValue(partes[1]);
                        }
                    } else if (arg.startsWith("--subir=")) {
                        String[] partes = arg.substring("--subir=".length()).split("::", 2);
                        //Varios archivos en un input multiple se envian separados por salto de linea
                        String rutas = java.util.Arrays.stream(partes[1].split(","))
                                .map(r -> java.nio.file.Paths.get(r.trim()).toAbsolutePath().toString())
                                .collect(java.util.stream.Collectors.joining("\n"));
                        driver.findElement(localizador(partes[0])).sendKeys(rutas);
                        Thread.sleep(1000);
                    } else if (arg.startsWith("--esperar=")) {
                        Thread.sleep(Long.parseLong(arg.substring("--esperar=".length())));
                    } else if (arg.equals("--nueva-ventana")) {
                        for (String ventana : driver.getWindowHandles()) {
                            driver.switchTo().window(ventana);
                        }
                        Thread.sleep(2000);
                    } else {
                        System.out.println("IGNORADA (opción desconocida)  ->  " + arg);
                        continue;
                    }
                    System.out.println("OK     ->  " + arg);
                } catch (Exception e) {
                    //Se reporta y se continua para poder inspeccionar en que estado quedo la pagina
                    System.out.println("FALLÓ  ->  " + arg + "  (" + e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()).split("\n")[0] + ")");
                }
            }

            System.out.println("\n=== PÁGINA");
            System.out.println("URL: " + driver.getCurrentUrl());
            System.out.println("Título: " + driver.getTitle());
            System.out.println("Ventanas abiertas: " + driver.getWindowHandles().size());

            System.out.println("\n=== ELEMENTOS (dentro de <main> o <body>)");
            System.out.println(js.executeScript(SCRIPT_ELEMENTOS));

            if (!verificaciones.isEmpty()) {
                System.out.println("\n=== VERIFICACIÓN DE SELECTORES");
                for (String selector : verificaciones) {
                    List<WebElement> encontrados = driver.findElements(localizador(selector));
                    String estado;
                    if (encontrados.isEmpty()) {
                        estado = "NO EXISTE";
                    } else {
                        estado = "OK (" + encontrados.size() + ", visible=" + encontrados.get(0).isDisplayed() + ")";
                    }
                    System.out.println(estado + "  ->  " + selector);
                }
            }
        } finally {
            driver.quit();
        }
    }

    private static By localizador(String selector) {
        return selector.startsWith("xpath=") ? By.xpath(selector.substring("xpath=".length())) : By.cssSelector(selector);
    }
}
