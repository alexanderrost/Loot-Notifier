package com.lootNotifier;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.game.ItemClient;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.item.ItemPrice;
import net.runelite.client.game.ItemManager;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
	name = "Loot Notifier Discord"
)
public class lootNotifierPlugin extends Plugin
{
	private static final String recievedDrop = "Valuable drop:";
	private int mostVal = 0;
	private int itemId = 0;

	@Inject
	private Client client;

	@Inject
	private lootNotifierConfig config;

	//Item manager handles all the items, getting id's and such
	@Inject
	private ItemManager itemManager;


	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
		if (gameStateChanged.getGameState()== GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","" + client.getWorld(), null);
			log.info(""+client.getWorld());
		}
	}

	// This will handle calculating the value of the drops
	@Subscribe
	public void onNpcLootReceived(NpcLootReceived npcLootReceived)
	{
		//Npc gives us the npc that we killed, used for keeping the name
		NPC npc = npcLootReceived.getNpc();
		// Collection stores all of our drops
		Collection<ItemStack> items = npcLootReceived.getItems();
		List<Integer> val = new ArrayList<>();
		// Itterate thru the items one by one
		for (ItemStack item : items)
		{
			//Get the id, find the price and add it to our list
			int id = item.getId();
			int totalVal = itemManager.getItemPriceWithSource(id, true);
			val.add(totalVal);
		}

		//Print the total sum to the players
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", "You killed: "+ npc.getName() + " for a value of: " + valueToPrint, null);
	}

	@Subscribe
	//TODO Fix drop
	public void	onChatMessage(ChatMessage msgFromChat)
	{
		String chatMessage = msgFromChat.getMessage();
		log.info(""+chatMessage);
		log.info(""+msgFromChat.getType());
		if(msgFromChat.getType() != ChatMessageType.PUBLICCHAT
		&& msgFromChat.getType() != ChatMessageType.SPAM
		&& msgFromChat.getType() != ChatMessageType.TRADE
		&& msgFromChat.getType() != ChatMessageType.FRIENDSCHATNOTIFICATION)
		{
			return;
		}

		if (chatMessage.equals(recievedDrop) && mostVal >= 200)
		{
			log.info("You got a valuable drop");
			String itemName = droppedItemName(chatMessage);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "","DROP:" + itemName + " value: " + mostVal, null);
		}
	}

	public String droppedItemName(String msg)
	{
		String[] str = msg.split(":");
		return str[1];
	}



	@Provides
	lootNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(lootNotifierConfig.class);
	}
}
