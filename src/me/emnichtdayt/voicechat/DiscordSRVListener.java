package me.emnichtdayt.voicechat;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;

public class DiscordSRVListener {
	@Subscribe
    public void accountsLinked(AccountLinkedEvent event) {
        if(VoiceChatMain.getSql().isSet(event.getPlayer())) {
        	VoiceChatMain.getSql().setID(event.getPlayer(), event.getUser().getIdLong());
        }else {
        	VoiceChatMain.getSql().createUser(event.getPlayer());
        	VoiceChatMain.getSql().setID(event.getPlayer(), event.getUser().getIdLong());
        }        
    }
}
