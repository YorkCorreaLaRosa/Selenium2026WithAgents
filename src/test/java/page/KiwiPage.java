package page;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class KiwiPage extends BasePage {
    public KiwiPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "cookies_accept")
    private WebElement btnAceptarCookies;

    @FindBy(xpath = "//div[@data-test='PlacePickerInputPlace-close']")
    private WebElement btnCerrarOrigen;

    @FindBy(xpath = "(//input[@data-test='SearchField-input'])[1]")
    private WebElement txtOrigen;

    @FindBy(xpath = "//div[@data-test='PlacePickerInput-origin']//div[@data-test='PlacePickerInputPlace']")
    private WebElement lblOrigenSeleccionado;


    //El banner de cookies no siempre aparece (ej. si ya se aceptaron): si no se muestra en 10s se continua sin fallar
    public void aceptarCookiesSiAparece() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(btnAceptarCookies))
                    .click();
        } catch (TimeoutException e) {
            System.out.println("No se mostró el banner de cookies");
        }
    }

    public void ingresarOrigen(String origen) {
        wait.until(ExpectedConditions.elementToBeClickable(btnCerrarOrigen)).click();
        wait.until(ExpectedConditions.visibilityOf(txtOrigen)).sendKeys(origen);

        //Esperar a que aparezca la opcion que contiene el texto buscado (no cualquier sugerencia por defecto)
        By opcionOrigen = By.xpath("//div[@data-test='PlacePickerRow-wrapper'][contains(., '" + origen + "')]");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(opcionOrigen)).click();
        } catch (TimeoutException e) {
            throw new AssertionError("No se encontró la opción de origen: " + origen, e);
        }
    }

    public String obtenerOrigenSeleccionado() {
        wait.until(ExpectedConditions.visibilityOf(lblOrigenSeleccionado));
        return lblOrigenSeleccionado.getText().trim();
    }
}
