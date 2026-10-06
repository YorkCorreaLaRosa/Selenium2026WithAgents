package page;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ValidarPagoPage extends BasePage {
    public ValidarPagoPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver,this);
    }

    //Se busca el titulo dentro del bloque de confirmacion: la pagina de pago anterior tambien tiene un h1 ("Payment Process")
    @FindBy(css = "#paso-confirmacion h1")
    private WebElement lblPayment;

    @FindBy(id = "order-id")
    private WebElement lblOrderId;

    @FindBy(id = "home")
    private WebElement btnHome;

    public String validarMensajePago(){
        wait.until(ExpectedConditions.visibilityOf(lblPayment));
        return lblPayment.getText().trim();
    }

    public String obtenerOrderId(){
        wait.until(ExpectedConditions.visibilityOf(lblOrderId));
        return lblOrderId.getText().trim();
    }

    public boolean validarPresenciaBoton(){
        return wait.until(ExpectedConditions.visibilityOf(btnHome)).isDisplayed();
    }
}
