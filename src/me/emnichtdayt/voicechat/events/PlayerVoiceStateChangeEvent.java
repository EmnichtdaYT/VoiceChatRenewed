package me.emnichtdayt.voicechat.events;

import me.emnichtdayt.voicechat.VoiceState;
import me.emnichtdayt.voicechat.entity.VoicePlayer;

public interface PlayerVoiceStateChangeEvent {
  void onPlayerVoiceStateChange(VoicePlayer player, VoiceState oldVoiceState, VoiceState newVoiceState, boolean getsKicked);
}
