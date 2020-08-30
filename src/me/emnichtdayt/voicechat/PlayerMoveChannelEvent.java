package me.emnichtdayt.voicechat;

public interface PlayerMoveChannelEvent {
	void onPlayerMoveChannel(VoicePlayer p, DCChannel oldChannel, DCChannel newChannel);
}
