package step;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import page.ToastAlertPage;

public class ToastAlertStep {

    ToastAlertPage toastAlertPage;

    public ToastAlertStep() {
        toastAlertPage= new ToastAlertPage(Hooks.getDriver());
    }

    @When("doy click al Toast")
    public void doyClickAlToast() {
        toastAlertPage.clickToast();
    }

    @Then("valido que se muestre el Toast {string}")
    public void validoQueSeMuestreElToast(String txtToast) {
        Assertions.assertTrue(toastAlertPage.validarToast(), "El toast no se mostró correctamente ");
        Assertions.assertTrue(toastAlertPage.obtenerTextoToast().contains(txtToast),
                "Texto del toast: " + toastAlertPage.obtenerTextoToast());
    }

    @When("doy click a la Alerta")
    public void doyClickALaAlerta() {
        toastAlertPage.clickAlerta();
    }

    @Then("valido que se muestre la alerta {string} y la acepto")
    public void validoQueSeMuestreLaAlertaYLaAcepto(String txtAlerta) {
        String textoAlerta = toastAlertPage.obtenerTextoAlertaYAceptar();
        Assertions.assertTrue(textoAlerta.contains(txtAlerta), "Texto de la alerta: " + textoAlerta);
    }
}
