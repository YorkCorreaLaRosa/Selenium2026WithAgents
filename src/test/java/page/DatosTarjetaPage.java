package page;

import base.BasePage;
import base.CardContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Set;

public class DatosTarjetaPage extends BasePage {

    private final CardContext cardContext;

    public DatosTarjetaPage(WebDriver driver, CardContext cardContext) {
        super(driver);
        this.cardContext = cardContext;
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "new-card-number")
    private WebElement lblNroTarjeta;

    @FindBy(id = "new-card-cvv")
    private WebElement lblCvv;

    @FindBy(id = "new-card-exp")
    private WebElement lblFechaExp;

    private String ventanaOriginal;

    public void cambiarVentana() {
        ventanaOriginal = driver.getWindowHandle();
        //La tarjeta se genera en una nueva ventana: esperar a que exista antes de cambiar
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> todasLasVentanas = driver.getWindowHandles();
        for (String ventana : todasLasVentanas) {
            if (!ventana.equalsIgnoreCase(ventanaOriginal)) {
                driver.switchTo().window(ventana);
            }
            System.out.println("Ventana: " + ventana);
        }
    }

    public void capturarTarjeta() {
        wait.until(ExpectedConditions.visibilityOf(lblNroTarjeta));
        cardContext.setTarjeta(lblNroTarjeta.getText().trim());
        System.out.println("Nro Tarjeta: " + cardContext.getTarjeta());
    }

    public void capturarCvv() {
        cardContext.setCvv(lblCvv.getText().trim());
        System.out.println("Cvv: " + cardContext.getCvv());
    }

    public void capturarFechaExp() {
        String[] fecha = lblFechaExp.getText().trim().split("/");
        cardContext.setMes(fecha[0]);
        cardContext.setAnio(fecha[1]);
        System.out.println("Mes: " + cardContext.getMes());
        System.out.println("Año: " + cardContext.getAnio());
    }

    public void cerrarVentana() {
        driver.close();
        driver.switchTo().window(ventanaOriginal);
    }


}
