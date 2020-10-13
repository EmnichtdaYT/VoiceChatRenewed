package me.emnichtdayt.voicechat;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;

public class DiscordSRVListener {
	private VoiceChatMain pl = VoiceChatMain.getInstance();
	@Subscribe
    public void accountsLinked(AccountLinkedEvent event) {
		
        if(pl.getSql().isSet(event.getPlayer())) {
        	pl.getSql().setID(event.getPlayer(), event.getUser().getIdLong());
        }else {
        	pl.getSql().createUser(event.getPlayer());
        	pl.getSql().setID(event.getPlayer(), event.getUser().getIdLong());
        }        
    }
}
