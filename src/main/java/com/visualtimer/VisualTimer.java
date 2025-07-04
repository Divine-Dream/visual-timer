package com.visualtimer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

@Slf4j
public class VisualTimer
{
    private AudioPlayer audioPlayer;
    private final String name;
    private final long durationMillis;
    private final String initialFormattedTime;

    private int remainingTicks;
    private int totalTicks;

    private boolean running = false;
    private boolean expired = false;
    private int flashCounter = 0;

    private VisualTimerPlugin plugin;

    // FLASH_SCREEN state
    private boolean hasFlashed = false;
    private int screenFlashCount = 0;
    private int screenFlashTicks = 0;

    public VisualTimer(String name, long durationMillis)
    {
        this.name = name;
        this.durationMillis = durationMillis;
        this.initialFormattedTime = formatTime(durationMillis);

        this.remainingTicks = (int) (durationMillis / 600);
        this.totalTicks = remainingTicks;
    }

    public void start()
    {
        if (!running && !expired)
        {
            running = true;
            flashCounter = 0;
            screenFlashCount = 0;
            screenFlashTicks = 0;
            hasFlashed = false;

            remainingTicks = (int)(durationMillis / 600) + 1; // Add 0.6s buffer
            totalTicks = remainingTicks;
        }
    }

    public void pause()
    {
        if (running)
        {
            tick(null);
            running = false;
        }
    }

    public void reset()
    {
        running = false;
        expired = false;
        flashCounter = 0;
        screenFlashCount = 0;
        screenFlashTicks = 0;
        hasFlashed = false;
    }

    public void stop()
    {
        running = false;
        expired = true;
        hasFlashed = false;
    }

    public void setPlugin(VisualTimerPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void tick(VisualTimerConfig config)
    {
        log.debug("Tick Start: expired={}, hasFlashed={}, screenFlashCount={}, screenFlashTicks={}",
                expired, hasFlashed, screenFlashCount, screenFlashTicks);

        if (running)
        {
            remainingTicks = Math.max(0, remainingTicks - 1);
            log.debug("Tick: remainingTicks={}, expired={}", remainingTicks, expired);

            if (remainingTicks == 0 && !expired)
            {
                expired = true;
                running = false;
                hasFlashed = false;
                screenFlashCount = 0;
                screenFlashTicks = 0;
                log.debug("Timer expired, starting screen flash");
                if (plugin.getConfig().playSoundOnExpire())
                {
                    playAlarmSound();
                }
            }
        }

        if (expired && !hasFlashed && config.expirationEffect() == VisualTimerConfig.ExpirationEffect.FLASH_SCREEN)
        {
            log.debug("Flash tick {} (flashCount={})", screenFlashTicks, screenFlashCount);

            boolean showFlash = screenFlashTicks % 2 == 0;
            plugin.setScreenFlashing(showFlash);
            log.debug("Flashing screen? {}", showFlash);

            if (!showFlash)
            {
                screenFlashCount++;
                log.debug("Incremented flash count to {}", screenFlashCount);
            }

            screenFlashTicks++;

            if (screenFlashCount >= 3)
            {
                log.debug("Stopping flash after 3 full flashes");
                plugin.setScreenFlashing(false);
                hasFlashed = true;
            }
        }
        else if (hasFlashed || config.expirationEffect() != VisualTimerConfig.ExpirationEffect.FLASH_SCREEN)
        {
            plugin.setScreenFlashing(false);
        }

        flashCounter++;
    }

    public String getFormattedTime()
    {
        long totalMillis = remainingTicks * 600L;
        return formatTime(totalMillis);
    }

    private String formatTime(long totalMillis)
    {
        long hours = totalMillis / (60 * 60 * 1000);
        long minutes = (totalMillis / (60 * 1000)) % 60;
        long seconds = (totalMillis / 1000) % 60;
        long tenths = (totalMillis % 1000) / 100;

        return String.format("%d:%02d:%02d.%d", hours, minutes, seconds, tenths);
    }

    public String getInitialFormattedTime()
    {
        return initialFormattedTime;
    }

    public boolean isRunning() { return running; }
    public boolean isExpired() { return expired; }
    public String getName() { return name; }

    public long getRemainingMillis()
    {
        return remainingTicks * 600L;
    }

    public int getFlashCounter()
    {
        return flashCounter++;
    }

    private void playAlarmSound()
    {
        if (!plugin.getConfig().playSoundOnExpire())
        {
            return;
        }

        try
        {
            // this doesn't play audio. I don't know why, i cba I'll update it later
            plugin.getAudioPlayer().play(getClass(), "/Alarm.wav", 0.8f); // 80% volume
        }
        catch (Exception e)
        {
            log.error("Failed to play alarm sound", e);
        }
    }
}