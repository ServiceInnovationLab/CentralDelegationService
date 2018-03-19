package delegations.cds.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

	private static Properties properties = new Properties();
	
	static {
        InputStream inputStream = Settings.class.getResourceAsStream("/settings.properties");
        if (inputStream == null) {
            throw new ExceptionInInitializerError("Unable to load settings");
        }
        try {
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
	
	private Settings() {
		
	}
	
	public static String getSetting(Setting setting) {
		return properties.getProperty(setting.getValue());
	}
}
