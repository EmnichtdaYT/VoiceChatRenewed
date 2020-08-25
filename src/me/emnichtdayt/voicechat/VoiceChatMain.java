package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class VoiceChatMain extends JavaPlugin{
	private static List<PlayerVoiceStateChangeEvent> voiceStateChangeListeners = new ArrayList<>();
	
	private static DiscordBot dcbot = null;
	
	private static VoiceChatSQL sql = null;
	
	public void onEnable() {
		
	}
	
	protected void fireVoiceStateChange(VoicePlayer player, VoiceState newVoiceState, boolean getsKicked) {
		for(PlayerVoiceStateChangeEvent listener : voiceStateChangeListeners) {
			listener.onPlayerVoiceStateChange(player, newVoiceState, getsKicked);
		}
	}
	
	public static void addVoiceChatListener(PlayerVoiceStateChangeEvent listener) {
		voiceStateChangeListeners.add(listener);
	}
}
