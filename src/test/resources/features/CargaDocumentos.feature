@Novus
Feature: Carga de documentos

  @CargaDocumentos
  Scenario: Subir una imagen
    Given ingreso a la pagina de carga de documentos de NovusTechnology
    When cargo el archivo "cucumber.png"
    Then valido que el archivo "cucumber.png" aparezca en la lista de archivos cargados
    And hago click en subir documentos y valido el mensaje "Documentos subidos correctamente"

  @CargaMultiple
  Scenario: Subir varios archivos a la vez
    Given ingreso a la pagina de carga de documentos de NovusTechnology
    When cargo los archivos "cucumber.png, test.csv" en la carga multiple
    Then valido que los archivos "cucumber.png, test.csv" aparezcan en la lista de archivos cargados
    And hago click en subir documentos y valido el mensaje "Documentos subidos correctamente"
