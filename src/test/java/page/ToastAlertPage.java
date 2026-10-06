package page;

import base.BasePage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ToastAlertPage extends BasePage {
    public ToastAlertPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "btn-toast")
    private WebElement btnToast;

    @FindBy(id = "toast-mensaje")
    private WebElement modalToast;

    @FindBy(id = "btn-alerta")
    private WebElement btnAlerta;


    public void clickToast() {
        wait.until(ExpectedConditions.elementToBeClickable(btnToast)).click();
    }

    public boolean validarToast() {
        wait.until(ExpectedConditions.visibilityOf(modalToast));
        return modalToast.isDisplayed();
    }

    public String obtenerTextoToast() {
        return modalToast.getText();
    }

    public void clickAlerta() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAlerta)).click();
    }

    //La pagina usa window.alert(): solo tiene boton Aceptar
    public String obtenerTextoAlertaYAceptar() {
        Alert alerta = wait.until(ExpectedConditions.alertIsPresent());
        String texto = alerta.getText();
        alerta.accept();
        return texto;
    }

}
