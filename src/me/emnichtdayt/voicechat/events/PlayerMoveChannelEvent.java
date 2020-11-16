package me.emnichtdayt.voicechat.events;

import me.emnichtdayt.voicechat.entity.DCChannel;
import me.emnichtdayt.voicechat.entity.VoicePlayer;

public interface PlayerMoveChannelEvent {
	void onPlayerMoveChannel(VoicePlayer p, DCChannel oldChannel, DCChannel newChannel);
}
