package me.emnichtdayt.voicechat;

import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VoiceChatTimer extends BukkitRunnable{
	VoiceChatLogic l;
	private String voiceDisconnectMessage;
	private VoiceChatMain pl;
	
	protected VoiceChatTimer(String voiceDisconnectMessage, VoiceChatMain pl) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
		this.pl = pl;
		l = new VoiceChatLogic(pl);
	}
	
	protected void rload(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}
	
	@Override
	public void run() {		
		while (!pl.kickList.isEmpty()) {
			pl.kickList.get(0).kickPlayer(voiceDisconnectMessage);
			pl.kickList.remove(0);
		}
		for (Iterator<? extends Player> iterator = pl.getServer().getOnlinePlayers().iterator(); iterator.hasNext();) {
			l.doLogic(iterator);
		}
	}

}
