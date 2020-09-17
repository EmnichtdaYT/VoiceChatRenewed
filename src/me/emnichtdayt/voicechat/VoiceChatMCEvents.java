package me.emnichtdayt.voicechat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VoiceChatMCEvents implements Listener {
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		long dcId = VoiceChatMain.getSql().getID(player);

		VoicePlayer playerVoice = null;
		
		if (dcId > 0) {
			playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.DISCONNECTED, dcId);
			VoiceChatMain.getPlayers().put(player, playerVoice);
		} else {
			playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.UNLINKED, -1);
			if (VoiceChatMain.getVoiceChatRequired()) {
				VoiceChatMain.fireVoiceStateChange(playerVoice, null, VoiceState.UNLINKED, true);
				player.kickPlayer("Voicechat register"); //TODO nachricht
			}else {
				VoiceChatMain.getPlayers().put(player, playerVoice);
			}
		}
		
		if(VoiceChatMain.getDcbot().isInWaitingChannel(playerVoice)) {
			playerVoice.setState(VoiceState.CONNECTED);
			VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.CONNECTED, false);
		}else if(VoiceChatMain.getVoiceChatRequired()){
			VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.CONNECTED, true);
			player.kickPlayer("Voicechat register"); //TODO nachricht
		}else {
			VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.CONNECTED, false);
		}
	}
}
