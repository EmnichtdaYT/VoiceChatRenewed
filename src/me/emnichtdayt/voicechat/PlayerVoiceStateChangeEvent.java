package me.emnichtdayt.voicechat;

public interface PlayerVoiceStateChangeEvent{
	void onPlayerVoiceStateChange(VoicePlayer player, VoiceState newVoiceState, boolean getsKicked);
}
