package me.emnichtdayt.voicechat;

import org.bukkit.scheduler.BukkitRunnable;

public class VoiceChatTimer extends BukkitRunnable{
	VoiceChatLogic l = new VoiceChatLogic();
	private String voiceDisconnectMessage;
	
	protected VoiceChatTimer(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}
	
	protected void rload(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}
	
	@Override
	public void run() {
		l.doLogic(voiceDisconnectMessage);
	}

}
