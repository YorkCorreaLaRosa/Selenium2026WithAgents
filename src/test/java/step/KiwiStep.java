package step;

import base.ConfigFileReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import page.KiwiPage;

import java.text.Normalizer;

public class KiwiStep {

    KiwiPage kiwiPage;

    public KiwiStep() {
        kiwiPage = new KiwiPage(Hooks.getDriver());
    }

    @Given("el usuario ingresa a la pagina de Kiwi")
    public void elUsuarioIngresaALaPaginaDeKiwi() {
        Hooks.getDriver().get(ConfigFileReader.getProp("url2"));
        kiwiPage.aceptarCookiesSiAparece();
    }

    @When("selecciono el origen {string}")
    public void seleccionoElOrigen(String origen) {
        kiwiPage.ingresarOrigen(origen);
    }

    @Then("valido que el origen seleccionado sea {string}")
    public void validoQueElOrigenSeleccionadoSea(String origen) {
        String origenSeleccionado = kiwiPage.obtenerOrigenSeleccionado();
        //La web muestra el nombre en español con tilde (ej. "Milán BGY"), se comparan sin tildes
        Assertions.assertTrue(quitarTildes(origenSeleccionado).contains(quitarTildes(origen)),
                "Origen esperado: " + origen + " | Origen seleccionado: " + origenSeleccionado);
    }

    private String quitarTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
