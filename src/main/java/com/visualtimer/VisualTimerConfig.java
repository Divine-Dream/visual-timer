package com.visualtimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("visualtimer")
public interface VisualTimerConfig extends Config
{
	@ConfigItem(
			keyName = "showExpiredTimers",
			name = "Show Expired Timers",
			description = "Whether to show timers after they expire"
	)
	default boolean showExpiredTimers()
	{
		return true;
	}

	@ConfigItem(
			keyName = "expirationEffect",
			name = "Expiration Alert",
			description = "Select how the timer alerts you when expired"
	)
	default ExpirationEffect expirationEffect()
	{
		return ExpirationEffect.FLASH_TEXT_FAST;
	}

	@ConfigItem(
			keyName = "playSoundOnExpire",
			name = "Play Alarm Sound",
			description = "Plays a sound when the timer expires"
	)
	default boolean playSoundOnExpire()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableWatchdogMessage",
			name = "Enable Watchdog Message",
			description = "When the timer expires, send an in game message which Watchdog can use for custom alerts."
	)
	default boolean enableWatchdogMessage()
	{
		return true;
	}

	enum ExpirationEffect
	{
		FLASH_TEXT_SLOW,
		FLASH_TEXT_FAST,
		FLASH_TIMER,
		FLASH_SCREEN,
		RAVE_TEXT,
		RAVE_TIMER
	}
}