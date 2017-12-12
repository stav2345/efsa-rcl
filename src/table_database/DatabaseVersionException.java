package table_database;

public class DatabaseVersionException extends Exception {
	private static final long serialVersionUID = -421412509108305013L;
	public DatabaseVersionException(String message) {
		super(message);
	}
}
