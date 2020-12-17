package me.emnichtdayt.voicechat.listener;

import java.awt.Color;

import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import me.emnichtdayt.voicechat.VoiceChatMain;
import me.emnichtdayt.voicechat.VoiceState;
import me.emnichtdayt.voicechat.entity.VoicePlayer;

public class DCmessageCreateEvent implements MessageCreateListener {
	private String voiceDisconnectMessage;

	private String embedTitle;

	private String connectedMessage;
	private String codeInvalid;
	private String noCode;
	private String color;

	private VoiceChatMain pl = VoiceChatMain.getInstance();

	public DCmessageCreateEvent(String voiceDisconnectMessage, String embedTitle, String connectedMessage,
			String codeInvalid, String noCode, String color) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;

		this.embedTitle = embedTitle;
		this.connectedMessage = connectedMessage;
		this.codeInvalid = codeInvalid;
		this.noCode = noCode;
		this.color = color;
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		if (!event.isPrivateMessage() || event.getMessageAuthor().isBotUser()
				|| event.getMessageAuthor().isYourself()) {
			return;
		}
		int code = 0;
		try {
			code = Integer.parseInt(event.getMessage().getContent());
		} catch (NumberFormatException exc) {

		}

		if (String.valueOf(code).length() != 4) {
			EmbedBuilder noCodeBedBuild = new EmbedBuilder();

			noCodeBedBuild.setColor(hexToColor(color));

			noCodeBedBuild.addField(embedTitle, noCode);

			event.getMessage().getChannel().sendMessage(noCodeBedBuild);

			return;
		}

		if (pl.registerKeys.containsKey(code)) {
			EmbedBuilder invalidBedBuild = new EmbedBuilder();

			invalidBedBuild.setColor(hexToColor(color));
			invalidBedBuild.addField(embedTitle, codeInvalid);

			event.getMessage().getChannel().sendMessage(invalidBedBuild);

			return;
		}

		Player target = pl.registerKeys.get(code);
		VoicePlayer targetVoice;
		
		if (pl.getPlayers().containsKey(target)) {
			targetVoice = pl.getPlayers().get(target);
			VoiceState oldVoiceState = targetVoice.getState();
			targetVoice.setDIscordID(event.getMessageAuthor().getId());
			if (oldVoiceState == VoiceState.CONNECTED) {
				if (!pl.getDcbot().isInWaitingChannel(targetVoice)) {
					if (target.isOnline() && pl.getVoiceChatRequired()) {
						target.kickPlayer(voiceDisconnectMessage);
						pl.fireVoiceStateChange(targetVoice, oldVoiceState, VoiceState.DISCONNECTED, true);
					} else {
						pl.fireVoiceStateChange(targetVoice, oldVoiceState, VoiceState.DISCONNECTED, false);
					}
				}
			} else if (target.isOnline() && pl.getDcbot().isInWaitingChannel(targetVoice)) {
				pl.fireVoiceStateChange(targetVoice, oldVoiceState, VoiceState.CONNECTED, false);
			} else {
				pl.fireVoiceStateChange(targetVoice, oldVoiceState, VoiceState.DISCONNECTED, false);
			}
		} else {
			targetVoice = new VoicePlayer(target, VoiceState.DISCONNECTED, event.getMessageAuthor().getId());
			if (pl.getDcbot().isInWaitingChannel(targetVoice)) {
				pl.fireVoiceStateChange(targetVoice, VoiceState.UNLINKED, VoiceState.CONNECTED, false);
			} else {
				if (target.isOnline()) {
					pl.fireVoiceStateChange(targetVoice, VoiceState.UNLINKED, VoiceState.DISCONNECTED,
							false);
				} else {
					pl.fireVoiceStateChange(targetVoice, VoiceState.UNLINKED, VoiceState.DISCONNECTED, true);
				}
			}
		}

		EmbedBuilder conBedBuild = new EmbedBuilder();

		conBedBuild.setColor(hexToColor(color));

		conBedBuild.addField(embedTitle, connectedMessage);

		pl.registerKeys.remove(code);

		if (!pl.getSql().isSet(target)) {
			pl.getSql().createUser(target);
		}

		pl.getSql().setID(target, event.getMessageAuthor().getId());

		event.getMessage().getChannel().sendMessage(conBedBuild);

	}

	protected Color hexToColor(String value) {
		String digits;
		if (value.startsWith("#")) {
			digits = value.substring(1, Math.min(value.length(), 7));
		} else {
			digits = value;
		}
		String hstr = "0x" + digits;
		Color c;
		try {
			c = Color.decode(hstr);
		} catch (NumberFormatException nfe) {
			c = null;
		}
		return c;
	}

	public void rload(String voiceDisconnectMessage, String embedTitle, String connectedMessage, String codeInvalid,
			String noCode, String color) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;

		this.embedTitle = embedTitle;
		this.connectedMessage = connectedMessage;
		this.codeInvalid = codeInvalid;
		this.noCode = noCode;
		this.color = color;
	}

}
