package me.emnichtdayt.voicechat;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

public class DCServerVoiceChannelMemberLeaveListener implements ServerVoiceChannelMemberLeaveListener {

	protected static String voiceDisconnectMessage;

	protected DCServerVoiceChannelMemberLeaveListener(String voiceDisconnectMessage) {
		DCServerVoiceChannelMemberLeaveListener.voiceDisconnectMessage = voiceDisconnectMessage;
	}

	@Override
	public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
		Optional<ServerVoiceChannel> optionalNChannel = event.getNewChannel();
		VoicePlayer targetVoice = VoiceChatMain.getPlayerByID(event.getUser().getId());
		if (targetVoice != null) {
			if (targetVoice.getState() == VoiceState.CONNECTED) {
				if (!(optionalNChannel.isPresent() && optionalNChannel.get().getName().length() > 8
						&& !optionalNChannel.get().getName().substring(0, 9).equals("VoiceChat"))
						&& optionalNChannel.get().getId() != Long.parseLong(VoiceChatMain.getDcbot().getWaitingChannelID())) {
					Player target = targetVoice.getPlayer();
					if (VoiceChatMain.getVoiceChatRequired()&&target.hasPermission("VoiceChat.bypass")) {
						targetVoice.setState(VoiceState.DISCONNECTED);
						VoiceChatMain.fireVoiceStateChange(targetVoice, VoiceState.CONNECTED, VoiceState.DISCONNECTED,
								true);
						VoiceChatMain.kickList.add(target);
					} else {
						if (targetVoice.getCurrentChannel() != null) {
							DCChannel oldChannel = targetVoice.getCurrentChannel();
							targetVoice.getCurrentChannel().getUsers().remove(targetVoice);
							targetVoice.currentChannel = null;
							VoiceChatMain.firePlayerMoveChannel(targetVoice, oldChannel, null);
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

	protected void rload(String voiceDisconnectMessage) {
		DCServerVoiceChannelMemberLeaveListener.voiceDisconnectMessage = voiceDisconnectMessage;
	}

}
