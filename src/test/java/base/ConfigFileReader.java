package base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFileReader {

    private static final String PROPERTY_FILE_NAME = "config.properties";

    //Se carga una sola vez al usarse la clase por primera vez y se comparte en toda la ejecución
    private static final Properties PROPERTIES = cargarPropiedades();

    private ConfigFileReader() {
    }

    private static Properties cargarPropiedades() {
        Properties properties = new Properties();
        try (InputStream input = ConfigFileReader.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME)) {
            if (input == null) {
                throw new IllegalStateException("No se encontró " + PROPERTY_FILE_NAME + " en el classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar " + PROPERTY_FILE_NAME, e);
        }
        return properties;
    }

    public static String getProp(String keyName) {
        String valor = PROPERTIES.getProperty(keyName);
        if (valor == null) {
            throw new IllegalStateException("No existe la propiedad '" + keyName + "' en " + PROPERTY_FILE_NAME);
        }
        return valor.trim();
    }
}
