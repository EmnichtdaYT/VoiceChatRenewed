package me.emnichtdayt.voicechat;

import java.util.ArrayList;

public class DCChannel {
	private long id;
	ArrayList<VoicePlayer> users = new ArrayList<VoicePlayer>();
	
	protected DCChannel(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
}
