package com.visualtimer;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.audio.AudioPlayer;

import java.awt.image.BufferedImage;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;

@Slf4j
@PluginDescriptor(name = "Visual Timer")
public class VisualTimerPlugin extends Plugin
{
	private AudioPlayer audioPlayer;
	@Inject private Client client;
	@Inject private VisualTimerConfig config;
	@Inject private VisualTimerOverlayManager overlayManager;
	@Inject private VisualTimerPanel visualTimerPanel;
	@Inject private ClientToolbar clientToolbar;
	@Inject private VisualTimerScreenOverlay screenOverlay;

	private NavigationButton navButton;

	private boolean screenFlashing = false;
	private boolean screenFlashVisible = false;
	private int flashTickCounter = 0;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Visual Timer started!");

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Visual Timer")
				.icon(icon)
				.panel(visualTimerPanel)
				.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(screenOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Visual Timer stopped!");
		clientToolbar.removeNavigation(navButton);
		overlayManager.removeAllTimers();
		overlayManager.remove(screenOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		for (VisualTimerOverlay overlay : overlayManager.getActiveOverlays())
		{
			overlay.getTimer().tick(config);
		}

		if (screenFlashing)
		{
			screenFlashVisible = true;
		}
		else
		{
			screenFlashVisible = false;
			flashTickCounter = 0;
		}

		// ✅ Update the panel preview timer
		visualTimerPanel.updatePreviewTimer();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
//		if (event.getGameState() == GameState.LOGGED_IN)
//		{
//			client.addChatMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, "", "Visual Timer is ready.", null);
//		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != RUNELITE_OVERLAY)
			return;

		for (VisualTimerOverlay overlay : overlayManager.getActiveOverlays())
		{
			if (event.getMenuTarget().contains("Timer"))
			{
				switch (event.getMenuEntry().getOption())
				{
					case "Start":
						overlay.getTimer().start();
						break;
					case "Pause":
						overlay.getTimer().pause();
						break;
					case "Stop":
						overlay.getTimer().stop();
						break;
				}
				break;
			}
		}
	}

	public void setScreenFlashing(boolean flashing)
	{
		this.screenFlashing = flashing;
	}

	public boolean isScreenFlashing()
	{
		return screenFlashing;
	}

	public boolean isScreenFlashVisible()
	{
		return screenFlashVisible;
	}

	public Client getClient()
	{
		return client;
	}

	public VisualTimerConfig getConfig()
	{
		return config;
	}

	public AudioPlayer getAudioPlayer()
	{
		return audioPlayer;
	}

	@Provides
	VisualTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VisualTimerConfig.class);
	}
}
