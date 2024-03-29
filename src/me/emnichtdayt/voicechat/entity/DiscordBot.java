package me.emnichtdayt.voicechat.entity;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import me.emnichtdayt.voicechat.VoiceChatMain;
import me.emnichtdayt.voicechat.VoiceState;
import me.emnichtdayt.voicechat.listener.DCServerVoiceChannelMemberLeaveListener;
import me.emnichtdayt.voicechat.listener.DCmessageCreateEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;

import javax.swing.text.html.Option;

public class DiscordBot {
  private int nextChannel = 0;

  private String status = ".";
  private ActivityType statusType = ActivityType.PLAYING;

  private ChannelCategory category;
  private Server server;

  private String waitingChannelID;

  private final DiscordApi api;

  private final DCmessageCreateEvent messageListener;
  private final DCServerVoiceChannelMemberLeaveListener channelLeaveListener;

  private final String channelPrefix;

  private final VoiceChatMain pl = VoiceChatMain.getInstance();

  public DiscordBot(
    String token,
    String server,
    String category,
    String waitingChannelID,
    ActivityType statusType,
    String status,
    String voiceDisconnectMessage,
    String embedTitle,
    String connectedMessage,
    String codeInvalid,
    String noCode,
    String color,
    String channelPrefix
  ) {
    api = new DiscordApiBuilder().setToken(token).login().join();

    this.channelPrefix = channelPrefix;

    this.setStatus(status);
    this.setStatusType(statusType);

    this.setWaitingChannelID(waitingChannelID);

    if (api.getServerById(server).isPresent()) {
      this.server = api.getServerById(server).get();
      if (api.getChannelCategoryById(category).isPresent()) {
        this.category = api.getChannelCategoryById(category).get();
      }
    }else{
      pl.getLogger().severe("[VoiceChat] The Discord server id supplied in the config is incorrect!");
    }

    messageListener = new DCmessageCreateEvent(voiceDisconnectMessage, embedTitle, connectedMessage, codeInvalid, noCode, color);
    channelLeaveListener = new DCServerVoiceChannelMemberLeaveListener(voiceDisconnectMessage, channelPrefix, category);
    api.addListener(messageListener);
    api.addListener(channelLeaveListener);
  }

  public void rloadVoiceDisconnectMessage(String voiceDisconnectMessage, String embedTitle, String connectedMessage, String codeInvalid, String noCode, String color) {
    messageListener.rload(voiceDisconnectMessage, embedTitle, connectedMessage, codeInvalid, noCode, color);
    channelLeaveListener.rload(voiceDisconnectMessage);
  }

  /**
   * getCategory() returns the ChannelCategory where all the channels will be
   * created
   *
   * @return ChannelCategory category
   */
  public ChannelCategory getCategory() {
    return category;
  }

  /**
   * getServer() returns the Discord server the bot is set to operate at
   *
   * @return Server server
   */
  public Server getServer() {
    return server;
  }

  /**
   * getStatus() returns the status displayed in Discord for the Discord Bot
   *
   * @return String status
   */
  public String getStatus() {
    return status;
  }

  /**
   * setStatus(String status) sets the status displayed in Discord for the Discord
   * Bot
   *
   * @param String status
   */
  public void setStatus(String status) {
    this.status = status;
    api.updateActivity(statusType, status);
  }

  /**
   * getStatusType() returns the ActivityType the Discord bot is currently set to.
   * For example: Playing, Watching, Streaming
   *
   * @return ActivityType statusType
   */
  public ActivityType getStatusType() {
    return statusType;
  }

  /**
   * setStatusType(ActivityType statusType) sets the Activity for the Discord bot.
   * For example: Playing, Watching, Streaming
   *
   * @param ActivityType statusType
   */
  public void setStatusType(ActivityType statusType) {
    this.statusType = statusType;
    api.updateActivity(statusType, status);
  }

  /**
   * getChannelByName(String name) returns the first registered DCChannel with the
   * given name. If no Channel was found it returns null
   *
   * @param String name
   * @return DCChannel channelVC
   */
  public DCChannel getChannelByName(String name) {
    for (ServerVoiceChannel channel : api.getServerVoiceChannelsByName(name)) {
      for (DCChannel channelVC : pl.getChannels()) {
        if (channelVC.getId() == channel.getId()) {
          return channelVC;
        }
      }
    }
    return null;
  }

  public DCChannel createNewUserVoiceChat() {
    ServerVoiceChannelBuilder nChan = new ServerVoiceChannelBuilder(getServer());
    nChan.setAuditLogReason("VoiceChat-customChannel");
    nChan.setName(channelPrefix + "-" + nextChannel);
    PermissionsBuilder nPerm = new PermissionsBuilder();
    nPerm.setDenied(PermissionType.READ_MESSAGES);
    nPerm.setDenied(PermissionType.CONNECT);
    nChan.addPermissionOverwrite(getServer().getEveryoneRole(), nPerm.build());
    nChan.setCategory(category);

    CompletableFuture<ServerVoiceChannel> futureChann = nChan.create();

    nextChannel++;

    DCChannel dcchann = null;

    try {
      dcchann = new DCChannel(futureChann.get().getId());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    if (dcchann != null) {
      pl.getChannels().add(dcchann);
    }

    return dcchann;
  }

  /**
   * createCustomChannel(String name) creates a new Discord Channel and returns
   * the DCChannel object
   *
   * @param String name
   * @return DCChannel dcchann
   */
  public DCChannel createCustomChannel(String name) {
    ServerVoiceChannelBuilder nChan = new ServerVoiceChannelBuilder(getServer());
    nChan.setAuditLogReason("VoiceChat-customChannel");
    nChan.setName(channelPrefix + "-" + name);
    PermissionsBuilder nPerm = new PermissionsBuilder();
    nPerm.setDenied(PermissionType.READ_MESSAGES);
    nPerm.setDenied(PermissionType.CONNECT);
    nChan.addPermissionOverwrite(getServer().getEveryoneRole(), nPerm.build());
    nChan.setCategory(category);

    CompletableFuture<ServerVoiceChannel> futureChann = nChan.create();

    DCChannel dcchann = null;

    try {
      dcchann = new DCChannel(futureChann.get().getId());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    if (dcchann != null) {
      pl.getChannels().add(dcchann);
    }

    return dcchann;
  }

  public void movePlayer(VoicePlayer target, DCChannel channel) {
    try {
      if (channel != null) {
        api.getUserById(target.getDiscordID()).get().move(api.getServerVoiceChannelById(channel.getId()).get());
      } else {
        api.getUserById(target.getDiscordID()).get().move(api.getServerVoiceChannelById(getWaitingChannelID()).get());
      }
    } catch (Exception e) {
      try {
        server.kickUserFromVoiceChannel(api.getUserById(target.getDiscordID()).get());
      } catch (Exception ex) { //Same problem as last one

      }
      VoiceChatMain pl = VoiceChatMain.getInstance();
      boolean getsKicked = pl.getVoiceChatRequired();
      if (getsKicked) {
        target.getPlayer().kickPlayer("[VoiceChat] Ooops, an error occured but it's not my fault! Are you registered? Is the information in the config correct?");
      }
      pl.fireVoiceStateChange(target, target.getState(), VoiceState.DISCONNECTED, getsKicked);
      target.disconnect();
    }
  }

  public void instantDeleteChannelFromDC(DCChannel dcChannel) {
    pl.getChannels().remove(dcChannel);
    Optional<ServerVoiceChannel> channel = api.getServerVoiceChannelById(dcChannel.getId());

    channel.ifPresent(ServerChannel::delete);
  }

  /**
   * Löscht den Kanal final. Löscht ihn auch aus channels arrL !!!kümmert sich
   * aber nicht darum dass alle user raus geschoben werden!!!
   *
   * @param DCChannel dcChannel
   */
  protected void deleteChannelFromDC(DCChannel dcChannel) {
    pl.getChannels().remove(dcChannel);
    Optional<ServerVoiceChannel> channel = api.getServerVoiceChannelById(dcChannel.getId());
    VoiceChatMain
      .getInstance()
      .getServer()
      .getScheduler()
      .scheduleSyncDelayedTask(
        VoiceChatMain.getInstance(),
              () -> channel.ifPresent(ServerChannel::delete),
        20L
      );
  }

  /**
   * isInWaitingChannel(VoicePlayer player) returns if the player is in the
   * waiting channel
   *
   * @param VoicePlayer player
   * @return isInWaitingChannel
   */
  public boolean isInWaitingChannel(VoicePlayer player) {
    Optional<ServerVoiceChannel> waitingChannelOptional = api.getServerVoiceChannelById(getWaitingChannelID());
    if(!waitingChannelOptional.isPresent()){
      pl.getLogger().severe("Waiting channel id in config is wrong!");
      return false;
    }
    return waitingChannelOptional.get().getConnectedUserIds().contains(player.getDiscordID());
  }

  /**
   * getWaitingChannelID() returns the waiting channel discord id - if you want to
   * move a player to the waiting channel just use VoicePlayer.moveTo(null)
   *
   * @return waitingChannelID
   */
  public String getWaitingChannelID() {
    return waitingChannelID;
  }

  private void setWaitingChannelID(String waitingChannelID) {
    this.waitingChannelID = waitingChannelID;
  }

  /**
   * getChannelPrefix() returns the String in front of the name of the discord channels
   *
   * @return channelPrefix
   */
  public String getChannelPrefix() {
    return channelPrefix;
  }
}
