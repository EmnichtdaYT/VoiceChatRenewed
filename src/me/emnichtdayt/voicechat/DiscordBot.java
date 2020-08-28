package me.emnichtdayt.voicechat;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;

public class DiscordBot {
	private String status = ".";
	private ActivityType statusType = ActivityType.PLAYING;
	
	DiscordApi api = null;
	
	protected DiscordBot(String token, ActivityType statusType, String status) {		
		api = new DiscordApiBuilder().setToken(token).login().join();
		
		this.setStatus(status);
		this.setStatusType(statusType);
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
}
