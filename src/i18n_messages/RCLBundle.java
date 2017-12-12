package i18n_messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class RCLBundle {

	private ResourceBundle bundle;
	
	public RCLBundle(ResourceBundle bundle) {
		this.bundle = bundle;
	}
	
	public String get(String key, String... values) {
		Message message = getMessage(key);
		
		if (values.length > 0)
			message.replace(values);

		return message.getMessage();
	}

	private Message getMessage(String key) {
		
		String message = null;
		try {
			message = bundle.getString(key);
			
		} catch (MissingResourceException e) {
			message = '!' + key + '!';
		}
		
		return new Message(key, message);
	}
}
