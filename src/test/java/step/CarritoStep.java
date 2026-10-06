package step;

import base.CardContext;
import base.ConfigFileReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import page.DatosPagoPage;
import page.DatosTarjetaPage;
import page.HomePage;
import page.ValidarPagoPage;

public class CarritoStep {

    HomePage homePage;
    DatosTarjetaPage datosTarjetaPage;
    DatosPagoPage datosPagoPage;
    ValidarPagoPage validarPagoPage;

    public CarritoStep() {
        CardContext cardContext = new CardContext();
        homePage = new HomePage(Hooks.getDriver());
        datosTarjetaPage = new DatosTarjetaPage(Hooks.getDriver(), cardContext);
        datosPagoPage = new DatosPagoPage(Hooks.getDriver(), cardContext);
        validarPagoPage = new ValidarPagoPage(Hooks.getDriver());
    }

    @Given("que accedo a la pagina de carrito de compras de NovusTechnology")
    public void queAccedoALaPaginaDeCarritoDeComprasDeNovusTechnology() {
        Hooks.getDriver().get(ConfigFileReader.getProp("url3"));
    }

    @When("doy click en generar tarjeta")
    public void doyClickEnGenerarTarjeta() {
        homePage.clickGenerarTarjeta();
    }

    @And("capturo los datos de la tarjeta")
    public void capturoLosDatosDeLaTarjeta() {
        datosTarjetaPage.cambiarVentana();
        datosTarjetaPage.capturarTarjeta();
        datosTarjetaPage.capturarCvv();
        datosTarjetaPage.capturarFechaExp();
        datosTarjetaPage.cerrarVentana();
    }

    @And("selecciono la cantidad de productos al carrito y le doy comprar")
    public void seleccionoLaCantidadDeProductosAlCarritoYLeDoyComprar() {
        homePage.seleccionarCantidad("5");
        homePage.clickComprar();
    }

    @And("agrego una cantidad {string} de productos al carrito")
    public void agregoUnaCantidadDeProductosAlCarrito(String cant) {
        homePage.seleccionarCantidad(cant);
        homePage.clickComprar();
    }

    @Then("ingreso los datos de la tarjeta")
    public void ingresoLosDatosDeLaTarjeta() {
        datosPagoPage.ingresarDatos();
    }

    @Then("validamos que el pago fue exitoso {string}")
    public void validamosQueElPagoFueExitoso(String txtPagoExitoso) {
        Assertions.assertEquals(txtPagoExitoso,validarPagoPage.validarMensajePago());
        Assertions.assertTrue(validarPagoPage.validarPresenciaBoton());
        Assertions.assertFalse(validarPagoPage.obtenerOrderId().isEmpty(), "No se generó el Order ID");

    }
}
