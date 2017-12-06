package i18n_messages;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	
	private static final String BUNDLE_NAME = "i18n_messages.messages";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);

	private Messages() {
	}
	
	public static String get(String key, String... values) {
		Message message = getMessage(key);
		
		if (values.length > 0)
			message.replace(values);

		return message.getMessage();
	}

	private static Message getMessage(String key) {
		
		String message = null;
		try {
			message = RESOURCE_BUNDLE.getString(key);
			
		} catch (MissingResourceException e) {
			message = '!' + key + '!';
		}
		
		return new Message(key, message);
	}
	
}
