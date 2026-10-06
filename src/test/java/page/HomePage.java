package page;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class HomePage extends BasePage {
    public HomePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "nav-generate-card-number")
    private WebElement btnGenerarTarjeta;

    @FindBy(name = "quantity")
    private WebElement cbCantidad;

    @FindBy(id = "btn-buy-now")
    private WebElement btnComprar;

    public void clickGenerarTarjeta() {
        wait.until(ExpectedConditions.elementToBeClickable(btnGenerarTarjeta));
        btnGenerarTarjeta.click();
    }

    public void seleccionarCantidad(String cantidad) {
        wait.until(ExpectedConditions.visibilityOf(cbCantidad));
        new Select(cbCantidad).selectByValue(cantidad);
    }

    public void clickComprar() {
        wait.until(ExpectedConditions.elementToBeClickable(btnComprar)).click();
    }
}
