@Carrito
Feature: Compra de un articulo con Tarjeta de Credito
  Yo como usuario de la tienda del laboratorio de NovusTechnology
  Quiero comprar un articulo con una tarjeta de credito
  Para ahorrar tiempo en hacer un tramite personal

  Background: Generar Tarjeta
    Given que accedo a la pagina de carrito de compras de NovusTechnology
    When doy click en generar tarjeta

  @Carrito1
  Scenario: Compro una cantidad fija de productos
    And capturo los datos de la tarjeta
    And selecciono la cantidad de productos al carrito y le doy comprar
    Then ingreso los datos de la tarjeta

  @Carrito2
  Scenario Outline: Compro una cantidad parametrizada de productos
    And capturo los datos de la tarjeta
    And agrego una cantidad "<cantidad>" de productos al carrito
    And ingreso los datos de la tarjeta
    Then validamos que el pago fue exitoso "Payment successfull!"
    Examples:
      | cantidad |
      | 3        |
      #| 5        |