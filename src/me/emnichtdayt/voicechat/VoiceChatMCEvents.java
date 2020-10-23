package me.emnichtdayt.voicechat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class VoiceChatMCEvents implements Listener {
	
	protected VoiceChatMCEvents(String voicechatInternalRegisterMessage, String voicechatExternalRegisterMessage,
			String notInWaitingChannelMessage) {
		this.voicechatInternalRegisterMessage = voicechatInternalRegisterMessage;
		this.voicechatExternalRegisterMessage = voicechatExternalRegisterMessage;
		this.notInWaitingChannelMessage = notInWaitingChannelMessage;
	}

	private String voicechatInternalRegisterMessage = "%VoiceChatRegister%";
	private String voicechatExternalRegisterMessage = "%VoiceChatRegister%";
	private String notInWaitingChannelMessage = "%notInWaitingChannel%";
	
	private VoiceChatMain pl = VoiceChatMain.getInstance();

	protected void rloadConfig(String voicechatInternalRegisterMessage, String voicechatExternalRegisterMessage,
			String notInWaitingChannelMessage) {
		this.voicechatInternalRegisterMessage = voicechatInternalRegisterMessage;
		this.voicechatExternalRegisterMessage = voicechatExternalRegisterMessage;
		this.notInWaitingChannelMessage = notInWaitingChannelMessage;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLoginEvent(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		long dcId = -1;
		dcId = pl.getSql().getID(player);

		final VoicePlayer playerVoice;

		if (dcId > 0) {
			playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.DISCONNECTED, dcId);
			pl.getPlayers().put(player, playerVoice);
		} else {
			playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.UNLINKED, -1);
			if (pl.getVoiceChatRequired() && !(player.hasPermission("VoiceChat.bypass") || player.isOp())) {
				VoiceChatMain.fireVoiceStateChange(playerVoice, null, VoiceState.UNLINKED, true);
				if (pl.isRegisterInternalMode()) {
					e.disallow(Result.KICK_OTHER, voicechatInternalRegisterMessage + pl.getNewRegisterCodeFor(player));
				} else {
					e.disallow(Result.KICK_OTHER, voicechatExternalRegisterMessage);
				}
				return;
			} else {
				pl.getPlayers().put(player, playerVoice);
			}

		}

		playerVoice.setAutomaticControlled(true);

		if (pl.getDcbot().isInWaitingChannel(playerVoice)) {
			playerVoice.setState(VoiceState.CONNECTED);
			VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.CONNECTED, false);
		} else if (pl.getVoiceChatRequired()) {
			if (!(player.hasPermission("VoiceChat.bypass") || player.isOp())) {
				VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.DISCONNECTED, true);
				e.disallow(Result.KICK_OTHER, notInWaitingChannelMessage);
			}
		}

	}

	@EventHandler
	public void onPlayerDisconnet(PlayerQuitEvent e) {
		if (pl.getPlayers().containsKey(e.getPlayer())) {
			VoicePlayer targetVoice = pl.getPlayers().get(e.getPlayer());
			if (targetVoice.getCurrentChannel() != null) {
				targetVoice.moveTo(null);
			}
			pl.getPlayers().remove(e.getPlayer());
			VoiceChatMain.fireVoiceStateChange(targetVoice, targetVoice.getState(), VoiceState.DISCONNECTED, true);
		}
	}
}
