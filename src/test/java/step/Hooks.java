package step;

import base.ConfigFileReader;
import com.aventstack.extentreports.service.ExtentService;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Hooks {

    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return DRIVER_THREAD_LOCAL.get();
    }

    //Datos del entorno para el reporte de Extent, tomados de la maquina donde se ejecuta (antes estaban fijos en extent.properties)
    @BeforeAll
    public static void registrarInfoDelSistema() {
        ExtentService.getInstance().setSystemInfo("Os", System.getProperty("os.name"));
        ExtentService.getInstance().setSystemInfo("Version", System.getProperty("os.version"));
        ExtentService.getInstance().setSystemInfo("Java", System.getProperty("java.version"));
        ExtentService.getInstance().setSystemInfo("Browser", ConfigFileReader.getProp("browser"));
    }

    @Before
    public void setUp() {

        String browser = ConfigFileReader.getProp("browser").toLowerCase();
        WebDriver driver;
        switch (browser) {
            case "chrome":
                driver = new ChromeDriver();
                break;

            case "firefox":
                driver = new FirefoxDriver();
                break;

            default:
                throw new RuntimeException("Browser no soportado: " + browser);
        }
        DRIVER_THREAD_LOCAL.set(driver);
        driver.manage().window().maximize();

        //Tiempo de espera implicito -> Es una espera genérica para la ejecución completa (driver.manage().timeouts().implicitlyWait(...))
        //No se usa: Selenium recomienda no mezclarla con la espera explícita de BasePage, porque los tiempos se vuelven
        //impredecibles (un fallo puede tardar implícita + explícita) y las esperas de invisibilidad se vuelven lentas.
        //Toda espera debe hacerse con wait.until(ExpectedConditions...) en los Page Objects.
    }

    @AfterStep
    public void screenshot(Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                final byte[] screenshot = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", scenario.getName());
            } catch (Exception e) {
                System.err.println("Error al tomar la captura de pantalla: " + e.getMessage());
            }
        }
    }


    @After
    public void tearDown() {
        //Si el @Before falló al crear el navegador no hay driver: se evita un NullPointerException que taparía el error real
        WebDriver driver = getDriver();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER_THREAD_LOCAL.remove();
        }
    }


}
