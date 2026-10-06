package page;

import base.BasePage;
import base.ConfigFileReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FormularioPage extends BasePage {
    public FormularioPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    private static final String CSV_FILE_PATH = "data/test.csv";

    @FindBy(id = "nombre-completo")
    private WebElement txtNombreCompleto;

    @FindBy(id = "correo")
    private WebElement txtCorreo;

    @FindBy(id = "telefono")
    private WebElement txtTelefono;

    @FindBy(id = "pais")
    private WebElement cboPais;

    @FindBy(id = "aceptar-terminos")
    private WebElement chkAceptarTerminos;

    @FindBy(id = "btn-enviar")
    private WebElement btnEnviar;

    @FindBy(id = "modal-resumen-titulo")
    private WebElement lblTituloResumen;


    public void ingresarUrl() {
        driver.get(ConfigFileReader.getProp("url1"));
    }

    public void ingresarNombreCompleto(String nombreCompleto) {
        wait.until(ExpectedConditions.visibilityOf(txtNombreCompleto));
        txtNombreCompleto.clear();
        txtNombreCompleto.sendKeys(nombreCompleto);
    }

    public void ingresarCorreo(String correo) {
        txtCorreo.clear();
        txtCorreo.sendKeys(correo);
    }

    public void ingresarTelefonoCorreo(String correo) {
        //Celular peruano: 9 digitos empezando con 9
        String numeroAleatorio = "9" + new Random().ints(8, 0, 10)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();

        txtTelefono.clear();
        txtTelefono.sendKeys(numeroAleatorio);
        ingresarCorreo(correo);
    }

    public void seleccionarGenero(String genero) {
        driver.findElement(By.xpath("//input[@name='genero'][@value='" + genero + "']")).click();
    }

    //Recibe las herramientas separadas por coma, ej. "Selenium,Cypress"
    public void seleccionarHerramientas(String herramientas) {
        for (String herramienta : herramientas.split(",")) {
            WebElement chkHerramienta = driver.findElement(By.xpath("//input[@name='herramientas'][@value='" + herramienta.trim() + "']"));
            if (!chkHerramienta.isSelected()) {
                chkHerramienta.click();
            }
        }
    }

    public void seleccionarPais(String pais) {
        new Select(cboPais).selectByValue(pais);
    }

    public void aceptarTerminos() {
        if (!chkAceptarTerminos.isSelected()) {
            chkAceptarTerminos.click();
        }
    }

    public void clickEnviar() {
        wait.until(ExpectedConditions.elementToBeClickable(btnEnviar)).click();
    }

    public String validarMensajeInfo() {
        wait.until(ExpectedConditions.visibilityOf(lblTituloResumen));
        return lblTituloResumen.getText();
    }

    //Campos del modal de resumen: nombre, correo, telefono, fecha, pais, genero, experiencia, herramientas, comentarios
    public String obtenerValorResumen(String campo) {
        WebElement valor = driver.findElement(By.cssSelector("[data-testid='resumen-" + campo + "']"));
        return valor.getText().trim();
    }

    //La validacion es la nativa de HTML5: el texto del mensaje depende del idioma del navegador
    public String validarMensajeError() {
        clickEnviar();
        String mensajeError = txtNombreCompleto.getAttribute("validationMessage");
        System.out.println("Error: " + mensajeError);
        return mensajeError;
    }

    public boolean esNombreCompletoObligatorioYVacio() {
        return (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validity.valueMissing;", txtNombreCompleto);
    }

    public List<Map<String, String>> leerDatosCsv() {
        InputStream input = getClass().getClassLoader().getResourceAsStream(CSV_FILE_PATH);
        if (input == null) {
            throw new IllegalStateException("No se encontró " + CSV_FILE_PATH + " en el classpath");
        }

        List<Map<String, String>> registros = new ArrayList<>();
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
             CSVParser csvParser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {
            for (CSVRecord csvRecord : csvParser) {
                registros.add(csvRecord.toMap());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error al leer " + CSV_FILE_PATH, e);
        }
        return registros;
    }
}
