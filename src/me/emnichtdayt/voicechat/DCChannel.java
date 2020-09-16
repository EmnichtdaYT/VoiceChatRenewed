package me.emnichtdayt.voicechat;

import java.util.ArrayList;

public class DCChannel {
	private long id;
	private ArrayList<VoicePlayer> users = new ArrayList<VoicePlayer>();
	private VoicePlayer host = null;
	private DiscordBot dc = VoiceChatMain.getDcbot();
	
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
		if(getUsers().contains(host)) {
			this.host = host;
		}else {
			throw new IllegalStateException("The specified host has to be in the Channel");
		}
	}
	
	public ArrayList<VoicePlayer> getUsers(){
		return users;
	}
	
	public void remove() {
		for(VoicePlayer target : getUsers()) {
			target.moveTo(null);
		}
		host = null;
		dc.deleteChannelFromDC(this);
		id = -1;
	}

	@Override
	public String toString() {
		return "DCChannel [id=" + id + ", users=" + users + ", host=" + host + ", dc=" + dc + "]";
	}
	
}
