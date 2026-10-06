package page;

import base.BasePage;
import base.CardContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class DatosPagoPage extends BasePage {

    private final CardContext cardContext;

    public DatosPagoPage(WebDriver driver, CardContext cardContext) {
        super(driver);
        this.cardContext = cardContext;
        PageFactory.initElements(driver,this);
    }

    //El id tiene un error de tipeo en la web (card_nmuber), replicado del original de Guru99
    @FindBy(id = "card_nmuber")
    private WebElement txtNroTarjeta;

    @FindBy(name = "month")
    private WebElement cbMes;

    @FindBy(name = "year")
    private WebElement cbAnio;

    @FindBy(id = "cvv_code")
    private WebElement txtCvv;

    @FindBy(name = "submit")
    private WebElement btnComprar;

    public void ingresarDatos(){
        wait.until(ExpectedConditions.visibilityOf(txtNroTarjeta)).sendKeys(cardContext.getTarjeta());
        //La tarjeta trae el mes con cero ("09") pero el combo usa valores sin cero ("9")
        new Select(cbMes).selectByValue(String.valueOf(Integer.parseInt(cardContext.getMes())));
        new Select(cbAnio).selectByValue(cardContext.getAnio());
        txtCvv.sendKeys(cardContext.getCvv());
        wait.until(ExpectedConditions.elementToBeClickable(btnComprar)).click();
    }

}
