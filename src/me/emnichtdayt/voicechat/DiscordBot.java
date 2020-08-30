package me.emnichtdayt.voicechat;

import java.util.Iterator;
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
	private String status = ".";
	private ActivityType statusType = ActivityType.PLAYING;
	
	private ChannelCategory category;
	private Server server;
	
	DiscordApi api = null;
	
	protected DiscordBot(String token, String server, String category, ActivityType statusType, String status) {		
		api = new DiscordApiBuilder().setToken(token).login().join();
		
		this.setStatus(status);
		this.setStatusType(statusType);
		
		if(api.getServerById(server).isPresent()) {
			this.server = api.getServerById(server).get();
			if(api.getChannelCategoryById(category).isPresent()) {
				this.category = api.getChannelCategoryById(category).get();
			}
		}
		
		
	}
	
	public ChannelCategory getCategory() {
		return category;
	}
	
	public Server getServer() {
		return server;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		api.updateActivity(statusType, status);
	}

	public ActivityType getStatusType() {
		return statusType;
	}

	public void setStatusType(ActivityType statusType) {
		this.statusType = statusType;
		api.updateActivity(statusType, status);
	}

	public DCChannel getChannelByName(String name) {
		
		for(Iterator<? extends ServerVoiceChannel> channels = api.getServerVoiceChannelsByName(name).iterator(); channels.hasNext();) {
			ServerVoiceChannel channel = channels.next();
			long id  = channel.getId();
			for(int i = 0; VoiceChatMain.getChannels().size()<i ; i++) {
				if(VoiceChatMain.getChannels().get(i).getId() == id) {
					return VoiceChatMain.getChannels().get(i);
				}
			}
		}
		return null;
	}
	public DCChannel createCustomChannel(String name) {
		ServerVoiceChannelBuilder nChan = new ServerVoiceChannelBuilder(getServer());
		nChan.setAuditLogReason("VoiceChat-customChannel");
		nChan.setName("VoiceChat-"+name);
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
		if(dcchann!=null) {
			VoiceChatMain.getChannels().add(dcchann);
		}
		
		return dcchann;
	}
	
	protected void movePlayer(VoicePlayer target, DCChannel channel) {
		try {
			api.getUserById(target.getDiscordID()).get().move(api.getServerVoiceChannelById(channel.getId()).get());
		} catch (Exception e) {
			try {
				server.kickUserFromVoiceChannel(api.getUserById(target.getDiscordID()).get());
			} catch (Exception ex) {}
			VoiceChatMain pl = VoiceChatMain.getInstance();
			pl.reloadConfig();
			boolean getsKicked = pl.getConfig().getBoolean("VoiceChat.isRequired");
			if(getsKicked) {				
				target.getPlayer().kickPlayer("[VoiceChat] Ein Fehler ist aufgetreten daher wurde dein VoiceChat deaktiviert.");
			}
			VoiceChatMain.fireVoiceStateChange(target, target.getState(), VoiceState.DISCONNECTED, getsKicked);
			target.disconnect();
		}
	}
}
