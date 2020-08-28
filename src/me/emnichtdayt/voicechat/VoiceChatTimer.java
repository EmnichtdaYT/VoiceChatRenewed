package me.emnichtdayt.voicechat;

import org.bukkit.scheduler.BukkitRunnable;

public class VoiceChatTimer extends BukkitRunnable{
	VoiceChatMain pl = null;
	
	protected VoiceChatTimer(VoiceChatMain pl) {
		this.pl = pl;
	}
	
	@Override
	public void run() {
		VoiceChatLogic.doLogic(pl);
	}

}
