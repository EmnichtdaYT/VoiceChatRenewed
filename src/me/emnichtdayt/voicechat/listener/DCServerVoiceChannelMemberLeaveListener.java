package me.emnichtdayt.voicechat.listener;

import java.util.Optional;
import me.emnichtdayt.voicechat.VoiceChatMain;
import me.emnichtdayt.voicechat.VoiceState;
import me.emnichtdayt.voicechat.entity.DCChannel;
import me.emnichtdayt.voicechat.entity.VoicePlayer;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

public class DCServerVoiceChannelMemberLeaveListener implements ServerVoiceChannelMemberLeaveListener {
  protected String voiceDisconnectMessage;

  private VoiceChatMain pl = VoiceChatMain.getInstance();
  private String channelPrefix;
  private String categoryId;

  public DCServerVoiceChannelMemberLeaveListener(String voiceDisconnectMessage, String channelPrefix, String categoryId) {
    this.voiceDisconnectMessage = voiceDisconnectMessage;
    this.channelPrefix = channelPrefix;
    this.categoryId = categoryId;
  }

  @Override
  public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {

    if(pl.isInConfigMode()){
      return;
    }

    Optional<ServerVoiceChannel> optionalNChannel = event.getNewChannel();
    VoicePlayer targetVoice = pl.getPlayerByID(event.getUser().getId());
    if (targetVoice == null) {
      return;
    }

    if (targetVoice.getState() != VoiceState.CONNECTED || !targetVoice.isAutomaticControlled()) {
      return;
    }

    if (!optionalNChannel.isPresent() || !optionalNChannel.get().getName().startsWith(channelPrefix) || !optionalNChannel.get().getCategory().isPresent() || !(optionalNChannel.get().getCategory().get().getId() + "").equals(categoryId)) {
      if (!optionalNChannel.isPresent() || optionalNChannel.get().getId() != Long.parseLong(pl.getDcbot().getWaitingChannelID())) {
        Player target = targetVoice.getPlayer();
        if (pl.getVoiceChatRequired() && !target.hasPermission("VoiceChat.bypass")) {
          targetVoice.setState(VoiceState.DISCONNECTED);
          pl.fireVoiceStateChange(targetVoice, VoiceState.CONNECTED, VoiceState.DISCONNECTED, true);
          pl.kickList.add(target);

        } else {
          if (targetVoice.getCurrentChannel() != null) {
            DCChannel oldChannel = targetVoice.getCurrentChannel();
            targetVoice.getCurrentChannel().getUsers().remove(targetVoice);
            targetVoice.currentChannel = null;
            pl.firePlayerMoveChannel(targetVoice, oldChannel, null);
            pl.fireVoiceStateChange(targetVoice, VoiceState.CONNECTED, VoiceState.DISCONNECTED, false);
            if (oldChannel.getUsers().size() < 2) {
              oldChannel.remove();
            } else if (oldChannel.getHost() != null && oldChannel.getHost().equals(targetVoice)) {
              oldChannel.setHost(oldChannel.getUsers().get(0));
            }
          }
        }
      }
    }
  }

  public void rload(String voiceDisconnectMessage) {
    this.voiceDisconnectMessage = voiceDisconnectMessage;
  }
}
