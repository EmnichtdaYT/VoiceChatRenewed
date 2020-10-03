package me.emnichtdayt.voicechat;

import java.awt.Color;

import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DCmessageCreateEvent implements MessageCreateListener {
	private String voiceDisconnectMessage = "%VoiceChatDisconnected%";
	
	private String embedTitle = "%VoiceChatName%";
	
	private String connectedMessage = "%ConnectedMessage%";	
	private String codeInvalid = "%CodeInvalid%";
	private String noCode = "%NoCode%";
	private String color = "#077d1f";

	protected DCmessageCreateEvent(String voiceDisconnectMessage, String embedTitle, String connectedMessage, String codeInvalid, String noCode, String color) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
		
		this.embedTitle = embedTitle;
		this.connectedMessage = connectedMessage;
		this.codeInvalid = codeInvalid;
		this.noCode = noCode;
		this.color = color;
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
					
					EmbedBuilder conBedBuild = new EmbedBuilder();
					
					conBedBuild.setColor(hexToColor(color));
					
					conBedBuild.addField(embedTitle, connectedMessage);

					VoiceChatMain.registerKeys.remove(code);

					VoiceChatMain.getSql().setID(target, event.getMessageAuthor().getId());

					event.getMessage().getChannel()
							.sendMessage(conBedBuild);

				} else {
					EmbedBuilder invalidBedBuild = new EmbedBuilder();
					
					invalidBedBuild.setColor(hexToColor(color));
					
					invalidBedBuild.addField(embedTitle, codeInvalid);
					
					event.getMessage().getChannel().sendMessage(invalidBedBuild);
				}
			} else {
				EmbedBuilder noCodeBedBuild = new EmbedBuilder();
				
				noCodeBedBuild.setColor(hexToColor(color));
				
				noCodeBedBuild.addField(embedTitle, noCode );
				
				event.getMessage().getChannel()
						.sendMessage(noCodeBedBuild);
																									
			}

		}
	}
	
	protected static final Color hexToColor( String value )
	{
		String digits;
		if ( value.startsWith( "#" ) )
		{
			digits = value.substring( 1, Math.min( value.length( ), 7 ) );
		}
		else
		{
			digits = value;
		}
		String hstr = "0x" + digits;
		Color c;
		try
		{
			c = Color.decode( hstr );
		}
		catch ( NumberFormatException nfe )
		{
			c = null;
		}
		return c;
	}

	protected void rload(String voiceDisconnectMessage, String embedTitle, String connectedMessage, String codeInvalid, String noCode, String color) {
		this.voiceDisconnectMessage = voiceDisconnectMessage;
		
		this.embedTitle = embedTitle;
		this.connectedMessage = connectedMessage;
		this.codeInvalid = codeInvalid;
		this.noCode = noCode;
		this.color = color;
	}

}
