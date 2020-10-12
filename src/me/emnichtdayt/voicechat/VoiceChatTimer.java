package me.emnichtdayt.voicechat;

import org.bukkit.scheduler.BukkitRunnable;

public class VoiceChatTimer extends BukkitRunnable{
	VoiceChatLogic l = new VoiceChatLogic();
	
	protected VoiceChatTimer() {
		
	}
	
	@Override
	public void run() {
		l.doLogic();
	}

}
