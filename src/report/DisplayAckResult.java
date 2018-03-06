package report;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import global_utils.Message;

public class DisplayAckResult {

	private String messageId;
	private List<Message> messages;
	private File ack;
	
	public DisplayAckResult(String messageId, List<Message> messages, File ack) {
		
		this.messages = messages;
		this.messageId = messageId;
		this.ack = ack;
		
		if (this.messages == null)
			this.messages = new ArrayList<>();
	}
	
	public DisplayAckResult(Message message) {
		this(null, Arrays.asList(message), null);
	}
	
	public DisplayAckResult(String messageId, Message message) {
		this(messageId, Arrays.asList(message), null);
	}
	
	public DisplayAckResult(String messageId, List<Message> messages) {
		this(messageId, messages, null);
	}
	
	public DisplayAckResult(List<Message> messages) {
		this(null, messages, null);
	}

	public DisplayAckResult(String messageId, File ack) {
		this(messageId, null, ack);
	}
	
	public void addMessage(Message m) {
		messages.add(m);
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	public File getDownloadedAck() {
		return ack;
	}
	public String getDcfMessageId() {
		return messageId;
	}
}
