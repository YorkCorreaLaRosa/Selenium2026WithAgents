package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {

    //public    -> Se puede acceder desde cualquier clase en cualquier paquete (package) y se puede modificar desde cualquier lugar
    //protected -> Se puede acceder desde la clase actual, cualquier clase dentro del mismo paquete (package) y cualquier subclase en cualquier paquete
    //static    -> Solo hay una isntancia de la variable y se puede acceder a ella desde cualquier otra parte del programa
    //final     -> Se puede acceder desde cualquier lugar, pero una vez que se le ha asignado un valor, no se puede cambiar

   protected WebDriver driver;

   protected WebDriverWait wait;

    public BasePage(WebDriver d){
        driver= d;

        //Tiempo de espera explicito -> El driver esperará hasta que se cumpla una determinada condición de espera en un lapso de tiempo
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }
}
//NOSUCHELEMENTEXCEPTION
//TIMEOUTEXCEPTION
//WEBDRIVEREXCEPTION
//NOSUCHEWINDOWEXCEPTION