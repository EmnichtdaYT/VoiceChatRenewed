package me.emnichtdayt.voicechat.listener;

import me.emnichtdayt.voicechat.VoiceChatMain;
import me.emnichtdayt.voicechat.VoiceState;
import me.emnichtdayt.voicechat.entity.VoicePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class VoiceChatMCEvents implements Listener {

  public VoiceChatMCEvents(String voicechatInternalRegisterMessage, String voicechatExternalRegisterMessage, String notInWaitingChannelMessage) {
    this.voicechatInternalRegisterMessage = voicechatInternalRegisterMessage;
    this.voicechatExternalRegisterMessage = voicechatExternalRegisterMessage;
    this.notInWaitingChannelMessage = notInWaitingChannelMessage;
  }

  private String voicechatInternalRegisterMessage;
  private String voicechatExternalRegisterMessage;
  private String notInWaitingChannelMessage;

  private VoiceChatMain pl = VoiceChatMain.getInstance();

  public void rloadConfig(String voicechatInternalRegisterMessage, String voicechatExternalRegisterMessage, String notInWaitingChannelMessage) {
    this.voicechatInternalRegisterMessage = voicechatInternalRegisterMessage;
    this.voicechatExternalRegisterMessage = voicechatExternalRegisterMessage;
    this.notInWaitingChannelMessage = notInWaitingChannelMessage;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerLoginEvent(PlayerLoginEvent e) {
    Player player = e.getPlayer();
    long dcId = -1;
    dcId = pl.getSql().getID(player);

    final VoicePlayer playerVoice;

    if (dcId > 0) {
      playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.DISCONNECTED, dcId);
      pl.getPlayers().put(player, playerVoice);
    } else {
      playerVoice = new VoicePlayer(e.getPlayer(), VoiceState.UNLINKED, -1);
      if (pl.getVoiceChatRequired() && !(player.hasPermission("VoiceChat.bypass") || player.isOp())) {
        pl.fireVoiceStateChange(playerVoice, null, VoiceState.UNLINKED, true);
        if (pl.isRegisterInternalMode()) {
          e.disallow(Result.KICK_OTHER, voicechatInternalRegisterMessage + pl.getNewRegisterCodeFor(player));
        } else {
          e.disallow(Result.KICK_OTHER, voicechatExternalRegisterMessage);
        }
        return;
      } else {
        pl.getPlayers().put(player, playerVoice);
      }
    }

    playerVoice.setAutomaticControlled(true);

    if (pl.getDcbot().isInWaitingChannel(playerVoice)) {
      playerVoice.setState(VoiceState.CONNECTED);
      pl.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.CONNECTED, false);
    } else if (pl.getVoiceChatRequired()) {
      if (!(player.hasPermission("VoiceChat.bypass") || player.isOp())) {
        pl.fireVoiceStateChange(playerVoice, VoiceState.DISCONNECTED, VoiceState.DISCONNECTED, true);
        e.disallow(Result.KICK_OTHER, notInWaitingChannelMessage);
      } else {
        playerVoice.setAutomaticControlled(false);
      }
    }
  }

  @EventHandler
  public void onPlayerDisconnet(PlayerQuitEvent e) {
    if (!pl.getPlayers().containsKey(e.getPlayer())) {
      return;
    }
    VoicePlayer targetVoice = pl.getPlayers().get(e.getPlayer());
    if (targetVoice.getCurrentChannel() != null) {
      targetVoice.moveTo(null);
    }
    pl.getPlayers().remove(e.getPlayer());
    pl.fireVoiceStateChange(targetVoice, targetVoice.getState(), VoiceState.DISCONNECTED, true);
  }
}
