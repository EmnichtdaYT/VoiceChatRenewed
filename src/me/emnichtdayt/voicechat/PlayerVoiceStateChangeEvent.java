package me.emnichtdayt.voicechat;

public interface PlayerVoiceStateChangeEvent{
	void onPlayerVoiceStateChange(VoicePlayer player, VoiceState oldVoiceState, VoiceState newVoiceState, boolean getsKicked);
}
