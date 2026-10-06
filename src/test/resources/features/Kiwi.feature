@Kiwi
Feature: E2E Kiwi

  Scenario: Realizamos la reserva de vuelos
    Given el usuario ingresa a la pagina de Kiwi
    When selecciono el origen "Milan"
    Then valido que el origen seleccionado sea "Milan"
