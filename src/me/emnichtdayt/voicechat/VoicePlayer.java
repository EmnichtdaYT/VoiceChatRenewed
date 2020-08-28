package me.emnichtdayt.voicechat;

import org.bukkit.entity.Player;

public class VoicePlayer {
	private Player player;
	private VoiceState state;
	private long discordID;
	private DCChannel currentChannel;
	private boolean isAutomaticControlled = true;
	
	protected VoicePlayer(Player player, VoiceState state, long discordID) {
		this.player = player;
		this.setState(state);
		this.discordID = discordID;
		
		VoiceChatMain.getPlayers().put(player, this);
	}

	public VoiceState getState() {
		return state;
	}

	protected void setState(VoiceState state) {
		this.state = state;
	}

	public Player getPlayer() {
		return player;
	}

	public long getDiscordID() {
		return discordID;
	}
	
	protected void setDIscordID(long discordID) {
		this.discordID = discordID;
	}
	
	protected void disconnect() {
		VoiceChatMain.getPlayers().remove(player);
		player = null;
	}

	public DCChannel getCurrentChannel() {
		return currentChannel;
	}

	public boolean isAutomaticControlled() {
		return isAutomaticControlled;
	}

	public void setAutomaticControlled(boolean isAutomaticControlled) {
		this.isAutomaticControlled = isAutomaticControlled;
	}
}
