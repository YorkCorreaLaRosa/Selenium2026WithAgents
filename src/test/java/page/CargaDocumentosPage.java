package page;

import base.BasePage;
import base.ConfigFileReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CargaDocumentosPage extends BasePage {
    public CargaDocumentosPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "archivo-unico")
    private WebElement inputArchivoUnico;

    @FindBy(id = "archivos-multiples")
    private WebElement inputArchivosMultiples;

    @FindBy(id = "btn-subir")
    private WebElement btnSubir;

    private final By lblNombresArchivos = By.cssSelector("[data-testid='archivo-nombre']");


    public void ingresarUrl() {
        driver.get(ConfigFileReader.getProp("url4"));
    }

    public void cargarArchivo(String nombreArchivo) {
        inputArchivoUnico.sendKeys(obtenerRutaArchivo(nombreArchivo));
    }

    //Selenium envia varios archivos a un input multiple separando las rutas con salto de linea
    public void cargarArchivosMultiples(List<String> nombresArchivos) {
        String rutas = nombresArchivos.stream()
                .map(this::obtenerRutaArchivo)
                .collect(Collectors.joining("\n"));
        wait.until(ExpectedConditions.visibilityOf(inputArchivosMultiples)).sendKeys(rutas);
    }

    //El archivo se busca en src/test/resources/data desde el classpath, sin depender del directorio de ejecucion
    private String obtenerRutaArchivo(String nombreArchivo) {
        URL recurso = getClass().getClassLoader().getResource("data/" + nombreArchivo);
        if (recurso == null) {
            throw new IllegalStateException("No se encontró data/" + nombreArchivo + " en el classpath");
        }
        try {
            return Paths.get(recurso.toURI()).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Ruta inválida para " + nombreArchivo, e);
        }
    }

    //Espera a que la lista muestre la cantidad indicada de archivos antes de leer sus nombres
    public List<String> obtenerArchivosCargados(int cantidadEsperada) {
        wait.until(ExpectedConditions.numberOfElementsToBe(lblNombresArchivos, cantidadEsperada));
        return driver.findElements(lblNombresArchivos).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<String> obtenerArchivosCargados() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(lblNombresArchivos, 0));
        return driver.findElements(lblNombresArchivos).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public void clickSubir() {
        wait.until(ExpectedConditions.elementToBeClickable(btnSubir)).click();
    }

    public boolean validarMensajeSubida(String mensaje) {
        By lblMensaje = By.xpath("//*[contains(text(),'" + mensaje + "')]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(lblMensaje)).isDisplayed();
    }
}
