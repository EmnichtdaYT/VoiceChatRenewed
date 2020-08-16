package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class VoiceChatMain extends JavaPlugin{
	private static List<PlayerVoiceStateChangeEvent> voiceStateChangeListeners = new ArrayList<>();
	
	public static void main(String[] args) {
		VoiceChatMain main = new VoiceChatMain(); //PL LOAD
		
		VoiceChatMain.addVoiceChatListener(new TestListener());
		
		main.fireVoiceStateChange(new VoicePlayer(),VoiceState.LINKED, false);
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
