package me.emnichtdayt.voicechat;

import org.bukkit.entity.Player;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DCmessageCreateEvent implements MessageCreateListener {
	private String voiceDisconnectMessage;

	protected DCmessageCreateEvent(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if (event.isPrivateMessage() && !event.getMessageAuthor().isBotUser()
				&& !event.getMessageAuthor().isYourself()) {
			int code = 0;
			try {
				code = Integer.parseInt(event.getMessage().getContent());
			} catch (NumberFormatException exc) {
				
			}
			if (String.valueOf(code).length() == 4) {
				if (VoiceChatMain.registerKeys.containsKey(code)) {
					Player target = VoiceChatMain.registerKeys.get(code);
					VoicePlayer targetVoice;
					if (VoiceChatMain.getPlayers().containsKey(target)) {
						targetVoice = VoiceChatMain.getPlayers().get(target);
						VoiceState oldVoiceState = targetVoice.getState();
						targetVoice.setDIscordID(event.getMessageAuthor().getId());
						if (oldVoiceState == VoiceState.CONNECTED) {
							if (!VoiceChatMain.getDcbot().isInWaitingChannel(targetVoice)) {
								if (target.isOnline() && VoiceChatMain.getVoiceChatRequired()) {
									target.kickPlayer(voiceDisconnectMessage);
									VoiceChatMain.fireVoiceStateChange(targetVoice, oldVoiceState,
											VoiceState.DISCONNECTED, true);
								} else {
									VoiceChatMain.fireVoiceStateChange(targetVoice, oldVoiceState,
											VoiceState.DISCONNECTED, false);
								}
							}
						} else if (target.isOnline() && VoiceChatMain.getDcbot().isInWaitingChannel(targetVoice)) {
							VoiceChatMain.fireVoiceStateChange(targetVoice, oldVoiceState, VoiceState.CONNECTED, false);
						} else {
							VoiceChatMain.fireVoiceStateChange(targetVoice, oldVoiceState, VoiceState.DISCONNECTED,
									false);
						}
					} else {
						targetVoice = new VoicePlayer(target, VoiceState.DISCONNECTED,
								event.getMessageAuthor().getId());
						if (VoiceChatMain.getDcbot().isInWaitingChannel(targetVoice)) {
							VoiceChatMain.fireVoiceStateChange(targetVoice, VoiceState.UNLINKED, VoiceState.CONNECTED,
									false);
						} else {
							if (target.isOnline()) {
								VoiceChatMain.fireVoiceStateChange(targetVoice, VoiceState.UNLINKED,
										VoiceState.DISCONNECTED, false);
							} else {
								VoiceChatMain.fireVoiceStateChange(targetVoice, VoiceState.UNLINKED,
										VoiceState.DISCONNECTED, true);
							}
						}
					}

					VoiceChatMain.registerKeys.remove(code);
					
					VoiceChatMain.getSql().setID(target, event.getMessageAuthor().getId());
					
					event.getMessage().getChannel().sendMessage("Got ya up and ready! Join the waiting channel and have fun playing!");

				} else {
					event.getMessage().getChannel().sendMessage("That code is invalid.");
				}
			} else {
				event.getMessage().getChannel()
						.sendMessage("Nope, sorry I only accept a 4 digit code for registration."); //TODO da überall config und emebds rein machen
			}

		}
	}

	public void rload(String voiceDisconnectMessage) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
	}

}
