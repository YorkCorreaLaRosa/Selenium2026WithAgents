@Novus
Feature: Automatización de Formulario

  @Formulario
  Scenario Outline: Ingreso de Datos
    Given ingreso a la pagina de NovusTechnology
    And ingreso los datos del formulario
      | Nombre completo | Género    | Herramientas     |
      | York Correa     | Masculino | Selenium,Cypress |
    And ingresamos el numero de telefono y correo electronico "<CorreoElectronico>"
    And seleccionamos el pais "<Pais>"
    And aceptamos los terminos
    Then hacemos click en el boton enviar y valida el mensaje "Información enviada"
    And validamos en el resumen que el campo "nombre" sea "York Correa"
    And validamos en el resumen que el campo "correo" sea "<CorreoElectronico>"
    And validamos en el resumen que el campo "pais" sea "<Pais>"
    And validamos en el resumen que el campo "genero" sea "Masculino"
    And validamos en el resumen que el campo "herramientas" sea "Selenium, Cypress"
    Examples:
      | CorreoElectronico   | Pais |
      | yorkcorrea@test.com | Perú |


  @Error
  Scenario: Validar mensaje de error
    Given ingreso a la pagina de NovusTechnology
    Then valido que el nombre completo sea obligatorio


  @Toast
  Scenario: Comprobar Toast de la pagina NovusTechnology
    Given ingreso a la pagina de NovusTechnology
    When doy click al Toast
    Then valido que se muestre el Toast "Notificación de práctica"

  @Alerta
  Scenario: Comprobar Alerta de la pagina NovusTechnology
    Given ingreso a la pagina de NovusTechnology
    When doy click a la Alerta
    Then valido que se muestre la alerta "Esto es una alerta de práctica" y la acepto

  @Csv
  Scenario: Ingreso datos mediante CSV
    Given ingreso a la pagina de NovusTechnology
    Then ingresamos la data del CSV y validamos el resumen de cada registro
