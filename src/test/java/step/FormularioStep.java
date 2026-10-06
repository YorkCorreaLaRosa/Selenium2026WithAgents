package step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import page.FormularioPage;

import java.util.List;
import java.util.Map;

public class FormularioStep {

    FormularioPage formularioPage;

    public FormularioStep(){
        formularioPage = new FormularioPage(Hooks.getDriver());
    }

    @Given("ingreso a la pagina de NovusTechnology")
    public void ingresoALaPaginaDeNovusTechnology() {
        formularioPage.ingresarUrl();
    }

    @And("ingreso los datos del formulario")
    public void ingresoLosDatosDelFormulario(DataTable dataTable) {
        Map<String, String> datos = dataTable.asMaps(String.class, String.class).get(0);
        formularioPage.ingresarNombreCompleto(datos.get("Nombre completo"));
        formularioPage.seleccionarGenero(datos.get("Género"));
        formularioPage.seleccionarHerramientas(datos.get("Herramientas"));
    }

    @And("ingresamos el numero de telefono y correo electronico {string}")
    public void ingresamosElNumeroDeTelefonoYCorreoElectronico(String email) {
        formularioPage.ingresarTelefonoCorreo(email);
    }

    @And("seleccionamos el pais {string}")
    public void seleccionamosElPais(String pais) {
        formularioPage.seleccionarPais(pais);
    }

    @And("aceptamos los terminos")
    public void aceptamosLosTerminos() {
        formularioPage.aceptarTerminos();
    }

    @Then("hacemos click en el boton enviar y valida el mensaje {string}")
    public void hacemosClickEnElBotonEnviarYValidaElMensaje(String txtInfoPersonal) {
        formularioPage.clickEnviar();
        Assertions.assertEquals(txtInfoPersonal, formularioPage.validarMensajeInfo());
    }

    @And("validamos en el resumen que el campo {string} sea {string}")
    public void validamosEnElResumenQueElCampoSea(String campo, String valorEsperado) {
        Assertions.assertEquals(valorEsperado, formularioPage.obtenerValorResumen(campo),
                "Valor incorrecto en el resumen para el campo: " + campo);
    }

    @Then("valido que el nombre completo sea obligatorio")
    public void validoQueElNombreCompletoSeaObligatorio() {
        String mensajeError = formularioPage.validarMensajeError();
        Assertions.assertTrue(formularioPage.esNombreCompletoObligatorioYVacio(),
                "El campo Nombre completo debería marcarse como obligatorio");
        Assertions.assertFalse(mensajeError == null || mensajeError.isEmpty(),
                "El navegador no mostró mensaje de validación");
    }

    @Then("ingresamos la data del CSV y validamos el resumen de cada registro")
    public void ingresamosLaDataDelCsvYValidamosElResumenDeCadaRegistro() {
        List<Map<String, String>> registros = formularioPage.leerDatosCsv();
        Assertions.assertFalse(registros.isEmpty(), "El CSV no tiene registros");

        for (Map<String, String> registro : registros) {
            //Se recarga la pagina por cada registro para partir de un formulario limpio
            formularioPage.ingresarUrl();
            formularioPage.ingresarNombreCompleto(registro.get("NombreCompleto"));
            formularioPage.ingresarCorreo(registro.get("Correo"));
            formularioPage.seleccionarPais(registro.get("Pais"));
            formularioPage.seleccionarGenero(registro.get("Género"));
            formularioPage.aceptarTerminos();
            formularioPage.clickEnviar();

            Assertions.assertEquals("Información enviada", formularioPage.validarMensajeInfo());
            Assertions.assertEquals(registro.get("NombreCompleto"), formularioPage.obtenerValorResumen("nombre"));
            Assertions.assertEquals(registro.get("Correo"), formularioPage.obtenerValorResumen("correo"));
            Assertions.assertEquals(registro.get("Pais"), formularioPage.obtenerValorResumen("pais"));
            Assertions.assertEquals(registro.get("Género"), formularioPage.obtenerValorResumen("genero"));
        }
    }
}
