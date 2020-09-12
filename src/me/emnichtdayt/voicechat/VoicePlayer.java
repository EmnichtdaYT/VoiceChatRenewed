package me.emnichtdayt.voicechat;

import org.bukkit.entity.Player;

public class VoicePlayer {
	private Player player;
	private VoiceState state;
	private long discordID;
	private DCChannel currentChannel = null;
	private boolean isAutomaticControlled = true;
	
	protected VoicePlayer(Player player, VoiceState state, long discordID) {
		this.player = player;
		this.setState(state);
		this.discordID = discordID;
		
		VoiceChatMain.getPlayers().put(player, this);
	}

	/**
	 * getState() returns the VoiceState of the player
	 */
	public VoiceState getState() {
		return state;
	}

	protected void setState(VoiceState state) {
		this.state = state;
	}

	/**
	 * getPlayer() returns the org.bukkit.entity.Player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * getDiscordID() gets the Discord id of the player
	 */
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
	
	/**
	 *  getCurrentChannel() gets the channel the player is in currently
	 */
	public DCChannel getCurrentChannel() {
		return currentChannel;
	}
	
	/**
	 * isAutomaticControlled() returns if the player gets controlled by the VoiceChat logic.
	 */
	public boolean isAutomaticControlled() {
		return isAutomaticControlled;
	}
	
	/**
	 * setAutomaticControlled(boolean isAutomaitcControlled) enables or disables VoiceChat's logic for that player.
	 */
	public void setAutomaticControlled(boolean isAutomaticControlled) {
		this.isAutomaticControlled = isAutomaticControlled;
	}
	
	/**
	 * moveTo(DCChannel channel) moves a player to another channel. NOTE: You have to disable automaticControlled first, otherwise VoiceChat might move the Player back to the Channel where it thinks it belongs to 
	 */
	public void moveTo(DCChannel channel) {
		if(getCurrentChannel().getHost().equals(this)) {
			for(VoicePlayer newHost : getCurrentChannel().getUsers()) {
				if(!newHost.equals(this)) {
					getCurrentChannel().setHost(newHost);
				}
			}
		}
		VoiceChatMain.firePlayerMoveChannel(this, currentChannel, channel);
		currentChannel.getUsers().remove(this);
		this.currentChannel = channel;
	}
}
