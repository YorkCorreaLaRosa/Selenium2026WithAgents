package step;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import page.CargaDocumentosPage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CargaDocumentosStep {

    CargaDocumentosPage cargaDocumentosPage;

    public CargaDocumentosStep() {
        cargaDocumentosPage = new CargaDocumentosPage(Hooks.getDriver());
    }

    @Given("ingreso a la pagina de carga de documentos de NovusTechnology")
    public void ingresoALaPaginaDeCargaDeDocumentosDeNovusTechnology() {
        cargaDocumentosPage.ingresarUrl();
    }

    @When("cargo el archivo {string}")
    public void cargoElArchivo(String nombreArchivo) {
        cargaDocumentosPage.cargarArchivo(nombreArchivo);
    }

    @Then("valido que el archivo {string} aparezca en la lista de archivos cargados")
    public void validoQueElArchivoAparezcaEnLaListaDeArchivosCargados(String nombreArchivo) {
        List<String> archivos = cargaDocumentosPage.obtenerArchivosCargados();
        Assertions.assertTrue(archivos.contains(nombreArchivo), "Archivos cargados: " + archivos);
    }

    //Los nombres de archivo se reciben separados por coma, ej. "cucumber.png, test.csv"
    @When("cargo los archivos {string} en la carga multiple")
    public void cargoLosArchivosEnLaCargaMultiple(String nombresArchivos) {
        cargaDocumentosPage.cargarArchivosMultiples(separarNombres(nombresArchivos));
    }

    @Then("valido que los archivos {string} aparezcan en la lista de archivos cargados")
    public void validoQueLosArchivosAparezcanEnLaListaDeArchivosCargados(String nombresArchivos) {
        List<String> esperados = separarNombres(nombresArchivos);
        List<String> archivos = cargaDocumentosPage.obtenerArchivosCargados(esperados.size());
        Assertions.assertEquals(esperados, archivos, "Los archivos de la lista no coinciden con los cargados");
    }

    @And("hago click en subir documentos y valido el mensaje {string}")
    public void hagoClickEnSubirDocumentosYValidoElMensaje(String mensaje) {
        cargaDocumentosPage.clickSubir();
        Assertions.assertTrue(cargaDocumentosPage.validarMensajeSubida(mensaje));
    }

    private List<String> separarNombres(String nombresArchivos) {
        return Arrays.stream(nombresArchivos.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
