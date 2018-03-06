package user;

import java.util.HashMap;

/**
 * Singleton user of the application
 * @author avonva
 *
 */
public class User extends DcfUser {

	// inner instance
	private static User user;
	
	private HashMap<String, String> data;
	
	/**
	 * Private constructor
	 */
	private User() {
		data = new HashMap<>();
	}

	/**
	 * Get an instance of the current user
	 */
	public static User getInstance() {

		// get the instance if it is present
		// or create it otherwise
		if ( user == null )
			user = new User();

		return user;
	}
	
	public void setData(HashMap<String, String> data) {
		this.data = data;
	}
	
	public HashMap<String, String> getData() {
		return data;
	}
	
	public void addData(String key, String value) {
		data.put(key, value);
	}
	
	public String getData(String key) {
		return data.get(key);
	}
}
