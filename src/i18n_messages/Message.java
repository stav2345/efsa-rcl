package i18n_messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

	private static final String VARIABLE_PLACEHOLDER = "%s";
	private static final String VARIABLE_PLACEHOLDER_REGEX = VARIABLE_PLACEHOLDER + "\\d+";  // as %s1, %s2, ...
	
	private String key;
	private String message;

	public Message(String key, String message) {
		this.key = key;
		this.message = message;
	}

	public String getKey() {
		return key;
	}
	public String getMessage() {
		return message;
	}

	public Message replace(String... values) {
		
		Pattern p = Pattern.compile(VARIABLE_PLACEHOLDER_REGEX);
		Matcher m = p.matcher(this.message);
		
		List<String> placeholders = new ArrayList<>();
		
		// for each match
		while(m.find()) {
			placeholders.add(m.group());
		}
		
		// order the placeholders and replace them in the order
		Collections.sort(placeholders);
		
		if (placeholders.size() != values.length) {
			System.err.println("The number of specified variables is not correct "
					+ "compared to the number of variables placeholders. Message: " + this.message + ", received variables " + values);
		}
		
		// replace what we can
		int minSize = Math.min(placeholders.size(), values.length);
		for (int i = 0; i < minSize; ++i) {
			String placeholder = placeholders.get(i);
			this.message = this.message.replaceAll(placeholder, values[i]);
		}
		
		return this;
	}

	@Override
	public String toString() {
		return this.getMessage();
	}
}