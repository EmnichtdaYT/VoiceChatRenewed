package me.emnichtdayt.voicechat.listener;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

import me.emnichtdayt.voicechat.VoiceChatMain;
import me.emnichtdayt.voicechat.VoiceState;
import me.emnichtdayt.voicechat.entity.DCChannel;
import me.emnichtdayt.voicechat.entity.VoicePlayer;

public class DCServerVoiceChannelMemberLeaveListener implements ServerVoiceChannelMemberLeaveListener {

	protected String voiceDisconnectMessage;
	
	private VoiceChatMain pl = VoiceChatMain.getInstance();

	public DCServerVoiceChannelMemberLeaveListener(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}

	@Override
	public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
		Optional<ServerVoiceChannel> optionalNChannel = event.getNewChannel();
		VoicePlayer targetVoice = pl.getPlayerByID(event.getUser().getId());
		if (targetVoice != null) {
			if (targetVoice.getState() == VoiceState.CONNECTED && targetVoice.isAutomaticControlled()) {
				if ((optionalNChannel.isPresent() && optionalNChannel.get().getName().length() > 8
						&& !optionalNChannel.get().getName().substring(0, 9).equals("VoiceChat"))||!optionalNChannel.isPresent()||(optionalNChannel.isPresent() && optionalNChannel.get().getName().length() <= 8)) {
					if ((optionalNChannel.isPresent() && optionalNChannel.get().getId() != Long.parseLong(pl.getDcbot().getWaitingChannelID()))||!optionalNChannel.isPresent()) {
						Player target = targetVoice.getPlayer();
						if (pl.getVoiceChatRequired() && !target.hasPermission("VoiceChat.bypass")) {
							targetVoice.setState(VoiceState.DISCONNECTED);
							VoiceChatMain.fireVoiceStateChange(targetVoice, VoiceState.CONNECTED,
									VoiceState.DISCONNECTED, true);
							pl.kickList.add(target);
						} else {
							if (targetVoice.getCurrentChannel() != null) {
								DCChannel oldChannel = targetVoice.getCurrentChannel();
								targetVoice.getCurrentChannel().getUsers().remove(targetVoice);
								targetVoice.currentChannel = null;
								pl.firePlayerMoveChannel(targetVoice, oldChannel, null);
								VoiceChatMain.fireVoiceStateChange(targetVoice, VoiceState.CONNECTED,
										VoiceState.DISCONNECTED, false);
								if (oldChannel.getUsers().size() < 2) {
									oldChannel.remove();
								} else if (oldChannel.getHost() != null && oldChannel.getHost().equals(targetVoice)) {
									oldChannel.setHost(oldChannel.getUsers().get(0));
								}
							}
						}
					}
				}
			}
		}
	}

	public void rload(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}

}
