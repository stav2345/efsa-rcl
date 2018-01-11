package user;

/**
 * Singleton user of the application
 * @author avonva
 *
 */
public class User extends DcfUser {

	// inner instance
	private static User user;
	
	/**
	 * Private constructor
	 */
	private User() {}

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
}
