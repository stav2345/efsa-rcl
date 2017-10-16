package acknowledge;

/**
 * Acknowledge obtained with {@link GetAck}
 * @author avonva
 *
 */
public class Ack {

	private FileState state;
	private AckLog log;
	
	public Ack(FileState state, AckLog log) {
		this.state = state;
		this.log = log;
	}
	
	public FileState getState() {
		return state;
	}
	
	public AckLog getLog() {
		return log;
	}
	
	public boolean isReady() {
		return state == FileState.READY;
	}
	
	@Override
	public String toString() {
		return "Ack: state=" + state + "; " + log;
	}
}
