package me.emnichtdayt.voicechat.entity;

import org.bukkit.entity.Player;

import me.emnichtdayt.voicechat.VoiceChatMain;
import me.emnichtdayt.voicechat.VoiceState;

public class VoicePlayer {
	@Override
	public String toString() {
		return "VoicePlayer [" + "state=" + state + ", discordID=" + discordID + ", isAutomaticControlled="
				+ isAutomaticControlled + "]";
	}

	private Player player;
	private VoiceState state;
	private long discordID;
	public DCChannel currentChannel = null;
	private boolean isAutomaticControlled = true;
	public boolean isInVoiceRegion = false;
	
	private VoiceChatMain pl = VoiceChatMain.getInstance();

	public VoicePlayer(Player player, VoiceState state, long discordID) {
		this.player = player;
		this.setState(state);
		this.discordID = discordID;

		pl.getPlayers().put(player, this);
	}

	/**
	 * getState() returns the VoiceState of the player
	 * 
	 * @return state
	 */
	public VoiceState getState() {
		return state;
	}

	public void setState(VoiceState state) {
		this.state = state;
	}

	/**
	 * getPlayer() returns the org.bukkit.entity.Player
	 * 
	 * @return player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * getDiscordID() gets the Discord id of the player
	 * 
	 * @return discordID
	 */
	public long getDiscordID() {
		return discordID;
	}

	public void setDIscordID(long discordID) {
		this.discordID = discordID;
	}

	protected void disconnect() {
		pl.getPlayers().remove(player);
		player = null;
	}

	/**
	 * getCurrentChannel() gets the channel the player is in currently returns null
	 * if in waiting channel or not in VoiceChat - use
	 * DiscordBot.isInWaitingChannel() if you want to know if someone is in the
	 * waiting channel
	 * 
	 * @return currentChannel
	 */
	public DCChannel getCurrentChannel() {
		return currentChannel;
	}

	/**
	 * isAutomaticControlled() returns if the player gets controlled by the
	 * VoiceChat logic.
	 * 
	 * @return isAutomaticControlled
	 */
	public boolean isAutomaticControlled() {
		return isAutomaticControlled;
	}

	/**
	 * setAutomaticControlled(boolean isAutomaitcControlled) enables or disables
	 * VoiceChat's logic for that player.
	 * 
	 * @param isAutomaticControlled
	 */
	public void setAutomaticControlled(boolean isAutomaticControlled) {
		this.isAutomaticControlled = isAutomaticControlled;
	}

	/**
	 * moveTo(DCChannel channel) moves a player to another channel. NOTE: You have
	 * to disable automaticControlled first, otherwise VoiceChat might move the
	 * Player back to the Channel where it thinks it belongs to. null is the waiting
	 * channel
	 * 
	 * @param channel
	 */
	
	public void moveTo(DCChannel channel) {
		if (getCurrentChannel() != null && getCurrentChannel().getHost() != null
				&& getCurrentChannel().getHost().equals(this)) {
			for (VoicePlayer newHost : getCurrentChannel().getUsers()) {
				if (!newHost.equals(this)) {
					getCurrentChannel().setHost(newHost);
				}
			}
		}
		DCChannel oldChannel = currentChannel;
		pl.firePlayerMoveChannel(this, oldChannel, channel);
		if (oldChannel != null) {
			oldChannel.getUsers().remove(this);
		}
		this.currentChannel = channel;
		if (channel != null) {
			currentChannel.getUsers().add(this);
		}

		pl.getDcbot().movePlayer(this, channel);
		
		pl.getServer().getScheduler().runTaskLaterAsynchronously(VoiceChatMain.getInstance(),
				new Runnable() {
					public void run() {
						if (oldChannel != null && oldChannel.getUsers().isEmpty()) {
							oldChannel.remove();
						}
					}
				}, 10);
	}
}
