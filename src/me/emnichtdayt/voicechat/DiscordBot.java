package me.emnichtdayt.voicechat;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;

public class DiscordBot {

	private int nextChannel = 0;

	private String status = ".";
	private ActivityType statusType = ActivityType.PLAYING;

	private ChannelCategory category;
	private Server server;

	private String waitingChannelID;

	private DiscordApi api = null;

	private DCmessageCreateEvent messageListener;
	private DCServerVoiceChannelMemberLeaveListener channelLeaveListener;

	protected DiscordBot(String token, String server, String category, String waitingChannelID, ActivityType statusType,
			String status, String voiceDisconnectMessage) {
		api = new DiscordApiBuilder().setToken(token).login().join();

		this.setStatus(status);
		this.setStatusType(statusType);

		this.setWaitingChannelID(waitingChannelID);

		if (api.getServerById(server).isPresent()) {
			this.server = api.getServerById(server).get();
			if (api.getChannelCategoryById(category).isPresent()) {
				this.category = api.getChannelCategoryById(category).get();
			}
		}

		messageListener = new DCmessageCreateEvent(voiceDisconnectMessage);
		channelLeaveListener = new DCServerVoiceChannelMemberLeaveListener(voiceDisconnectMessage);
		api.addListener(messageListener);
		api.addListener(channelLeaveListener);
	}

	protected void rloadVoiceDisconnectMessafe(String voiceDisconnectMessage) {
		messageListener.rload(voiceDisconnectMessage);
		channelLeaveListener.rload(voiceDisconnectMessage);
	}

	/**
	 * getCategory() returns the ChannelCategoty where all the channels will be
	 * created
	 * 
	 * @return ChannelCategory category
	 */
	public ChannelCategory getCategory() {
		return category;
	}

	/**
	 * getServer() returns the Discord server the bot is set to operate at
	 * 
	 * @return Server server
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * getStatus() returns the status displayed in Discord for the Discord Bot
	 * 
	 * @return String status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * setStatus(String status) sets the status displayed in Discord for the Discord
	 * Bot
	 * 
	 * @param String status
	 */
	public void setStatus(String status) {
		this.status = status;
		api.updateActivity(statusType, status);
	}

	/**
	 * getStatusType() returns the ActivityType the Discord bot is currently set to.
	 * For example: Playing, Watching, Streaming
	 * 
	 * @return ActivityType statusType
	 */
	public ActivityType getStatusType() {
		return statusType;
	}

	/**
	 * setStatusType(ActivityType statusType) sets the Activity for the Discord bot.
	 * For example: Playing, Watching, Streaming
	 * 
	 * @param ActivityType statusType
	 */
	public void setStatusType(ActivityType statusType) {
		this.statusType = statusType;
		api.updateActivity(statusType, status);
	}

	/**
	 * getChannelByName(String name) returns the first registered DCChannel with the
	 * given name. If no Channel was found it returns null
	 * 
	 * @param String name
	 * @return DCChannel channelVC
	 */
	public DCChannel getChannelByName(String name) {

		for (Iterator<? extends ServerVoiceChannel> channels = api.getServerVoiceChannelsByName(name)
				.iterator(); channels.hasNext();) {
			ServerVoiceChannel channel = channels.next();
			for (DCChannel channelVC : VoiceChatMain.getChannels()) {
				if (channelVC.getId() == channel.getId()) {
					return channelVC;
				}
			}
		}
		return null;
	}

	protected DCChannel createNewUserVoiceChat() {
		ServerVoiceChannelBuilder nChan = new ServerVoiceChannelBuilder(getServer());
		nChan.setAuditLogReason("VoiceChat-customChannel");
		nChan.setName("VoiceChat-" + nextChannel);
		PermissionsBuilder nPerm = new PermissionsBuilder();
		nPerm.setDenied(PermissionType.READ_MESSAGES);
		nPerm.setDenied(PermissionType.CONNECT);
		nChan.addPermissionOverwrite(getServer().getEveryoneRole(), nPerm.build());
		nChan.setCategory(category);

		CompletableFuture<ServerVoiceChannel> futureChann = nChan.create();

		nextChannel++;

		DCChannel dcchann = null;

		try {
			dcchann = new DCChannel(futureChann.get().getId());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (dcchann != null) {
			VoiceChatMain.getChannels().add(dcchann);
		}

		return dcchann;
	}

	/**
	 * createCustomChannel(String name) creates a new Discord Channel and returns
	 * the DCChannel object
	 * 
	 * @param String name
	 * @return DCChannel dcchann
	 */
	public DCChannel createCustomChannel(String name) {
		ServerVoiceChannelBuilder nChan = new ServerVoiceChannelBuilder(getServer());
		nChan.setAuditLogReason("VoiceChat-customChannel");
		nChan.setName("VoiceChat-" + name);
		PermissionsBuilder nPerm = new PermissionsBuilder();
		nPerm.setDenied(PermissionType.READ_MESSAGES);
		nPerm.setDenied(PermissionType.CONNECT);
		nChan.addPermissionOverwrite(getServer().getEveryoneRole(), nPerm.build());
		nChan.setCategory(category);

		CompletableFuture<ServerVoiceChannel> futureChann = nChan.create();

		DCChannel dcchann = null;

		try {
			dcchann = new DCChannel(futureChann.get().getId());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (dcchann != null) {
			VoiceChatMain.getChannels().add(dcchann);
		}

		return dcchann;
	}

	protected void movePlayer(VoicePlayer target, DCChannel channel) {
		try {
			if (channel != null) {
				api.getUserById(target.getDiscordID()).get().move(api.getServerVoiceChannelById(channel.getId()).get());
			} else {
				api.getUserById(target.getDiscordID()).get()
						.move(api.getServerVoiceChannelById(getWaitingChannelID()).get());
			}
		} catch (Exception e) {
			try {
				server.kickUserFromVoiceChannel(api.getUserById(target.getDiscordID()).get());
			} catch (Exception ex) {
			}
			VoiceChatMain pl = VoiceChatMain.getInstance();
			pl.reloadConfig();
			boolean getsKicked = pl.getConfig().getBoolean("VoiceChat.isRequired");
			if (getsKicked) {
				target.getPlayer()
						.kickPlayer("[VoiceChat] Ein Fehler ist aufgetreten daher wurde dein VoiceChat deaktiviert.");
			}
			VoiceChatMain.fireVoiceStateChange(target, target.getState(), VoiceState.DISCONNECTED, getsKicked);
			target.disconnect();
		}
	}

	protected void instantDeleteChannelFromDC(DCChannel dcChannel) {
		VoiceChatMain.getChannels().remove(dcChannel);
		Optional<ServerVoiceChannel> channel = api.getServerVoiceChannelById(dcChannel.getId());

		if (channel.isPresent()) {
			channel.get().delete();
		}

	}

	/**
	 * Löscht den Kanal final. Löscht ihn auch aus channels arrL !!!kümmert sich
	 * aber nicht darum dass alle user raus geschoben werden!!!
	 * 
	 * @param DCChannel dcChannel
	 */
	protected void deleteChannelFromDC(DCChannel dcChannel) {
		VoiceChatMain.getChannels().remove(dcChannel);
		Optional<ServerVoiceChannel> channel = api.getServerVoiceChannelById(dcChannel.getId());
		VoiceChatMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(VoiceChatMain.getInstance(),
				new Runnable() {

					public void run() {
						if (channel.isPresent()) {
							channel.get().delete();
						}
					}
				}, 20L);

	}

	/**
	 * isInWaitingChannel(VoicePlayer player) returns if the player is in the
	 * waiting channel
	 * 
	 * @param VoicePlayer player
	 * @return isInWaitingChannel
	 */
	public boolean isInWaitingChannel(VoicePlayer player) {
		return api.getServerVoiceChannelById(getWaitingChannelID()).get().getConnectedUserIds()
				.contains(player.getDiscordID());
	}

	/**
	 * getWaitingChannelID() returns the waiting channel discord id - if you want to
	 * move a player to the waiting channel just use VoicePlayer.moveTo(null)
	 * 
	 * @return waitingChannelID
	 */
	public String getWaitingChannelID() {
		return waitingChannelID;
	}

	private void setWaitingChannelID(String waitingChannelID) {
		this.waitingChannelID = waitingChannelID;
	}
}
