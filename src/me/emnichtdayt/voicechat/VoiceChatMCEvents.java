package me.emnichtdayt.voicechat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class VoiceChatMCEvents implements Listener {

	private static String voicechatInternalRegisterMessage = "%VoiceChatRegister%";
	private static String voicechatExternalRegisterMessage = "%VoiceChatRegister%";
	private static String notInWaitingChannelMessage = "%notInWaitingChannel%";

	protected static void rloadConfig(String voicechatInternalRegisterMessage, String voicechatExternalRegisterMessage,
			String notInWaitingChannelMessage) {
		VoiceChatMCEvents.voicechatInternalRegisterMessage = voicechatInternalRegisterMessage;
		VoiceChatMCEvents.voicechatExternalRegisterMessage = voicechatExternalRegisterMessage;
		VoiceChatMCEvents.notInWaitingChannelMessage = notInWaitingChannelMessage;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLoginEvent(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		long dcId = -1;
		dcId = VoiceChatMain.getSql().getID(player);

		final VoicePlayer playerVoice;

		if (dcId > 0) {
			playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.DISCONNECTED, dcId);
			VoiceChatMain.getPlayers().put(player, playerVoice);
		} else {
			playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.UNLINKED, -1);
			if (VoiceChatMain.getVoiceChatRequired() && !(player.hasPermission("VoiceChat.bypass") || player.isOp())) {
				VoiceChatMain.fireVoiceStateChange(playerVoice, null, VoiceState.UNLINKED, true);
				if (VoiceChatMain.isRegisterInternalMode()) {
					e.disallow(Result.KICK_OTHER, voicechatInternalRegisterMessage + VoiceChatMain.getNewRegisterCodeFor(player));
				} else {
					e.disallow(Result.KICK_OTHER, voicechatExternalRegisterMessage);
				}
				return;
			} else {
				VoiceChatMain.getPlayers().put(player, playerVoice);
			}

		}

		playerVoice.setAutomaticControlled(true);

		if (VoiceChatMain.getDcbot().isInWaitingChannel(playerVoice)) {
			playerVoice.setState(VoiceState.CONNECTED);
			VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.CONNECTED, false);
		} else if (VoiceChatMain.getVoiceChatRequired()) {
			if (!(player.hasPermission("VoiceChat.bypass") || player.isOp())) {
				VoiceChatMain.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.DISCONNECTED, true);
				e.disallow(Result.KICK_OTHER, notInWaitingChannelMessage);
			}
		}

	}

	@EventHandler
	public void onPlayerDisconnet(PlayerQuitEvent e) {
		if (VoiceChatMain.getPlayers().containsKey(e.getPlayer())) {
			VoicePlayer targetVoice = VoiceChatMain.getPlayers().get(e.getPlayer());
			if (targetVoice.getCurrentChannel() != null) {
				targetVoice.moveTo(null);
			}
			VoiceChatMain.getPlayers().remove(e.getPlayer());
			VoiceChatMain.fireVoiceStateChange(targetVoice, targetVoice.getState(), VoiceState.DISCONNECTED, true);
		}
	}
}
