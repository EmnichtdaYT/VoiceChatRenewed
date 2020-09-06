package me.emnichtdayt.voicechat;

import java.util.ArrayList;

public class DCChannel {
	private long id;
	ArrayList<VoicePlayer> users = new ArrayList<VoicePlayer>();
	private VoicePlayer host = null;
	
	protected DCChannel(long id) {
		this.id = id;
	}
	
	/**
	 * getId() gets the ID of the Channel in Discord
	 */
	public long getId() {
		return id;
	}

	/**
	 * getHost() gets the host of a Channel, if null its a System Channel like a WorldGuard Region for example
	 */
	public VoicePlayer getHost() {
		return host;
	}

	/**
	 * setHost(VoicePlayer host) defines a new Host, every person around that Player will be in the same VoiceChat. Can also be null if there is no such Player, for a phone call for example.
	 */
	public void setHost(VoicePlayer host) {
		this.host = host;
	}
}
