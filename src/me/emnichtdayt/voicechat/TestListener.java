package me.emnichtdayt.voicechat;

public class TestListener implements PlayerVoiceStateChangeEvent{

	@Override
	public void onPlayerVoiceStateChange(VoicePlayer player, VoiceState newVoiceState, boolean getsKicked) {
		System.out.println("FIRED :D " + newVoiceState.toString());
	}
	
}
