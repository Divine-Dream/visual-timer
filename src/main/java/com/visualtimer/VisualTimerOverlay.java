package com.visualtimer;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;

import javax.inject.Inject;
import java.awt.*;

public class VisualTimerOverlay extends Overlay
{
    private final Client client;
    private final VisualTimerPlugin plugin;
    private final VisualTimer timer;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public VisualTimerOverlay(Client client, VisualTimerPlugin plugin, VisualTimer timer)
    {
        this.client = client;
        this.plugin = plugin;
        this.timer = timer;

        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.MED);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPreferredLocation(new Point(0, 0));

        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, "Start", "Timer"));
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, "Pause", "Timer"));
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, "Stop", "Timer"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        if (timer == null || (timer.isExpired() && !plugin.getConfig().showExpiredTimers()))
        {
            return null;
        }

        panelComponent.setBackgroundColor(null); // reset background color every render

        long totalMillis = timer.getRemainingMillis();
        long hours = totalMillis / (1000 * 60 * 60);
        long minutes = (totalMillis / (1000 * 60)) % 60;
        long seconds = (totalMillis / 1000) % 60;
        long tenths = (totalMillis % 1000) / 100;

        String timeString = String.format("%d:%02d:%02d.%d", hours, minutes, seconds, tenths);

        Color timeColor = Color.WHITE;
        Color nameColor = Color.WHITE;
        Color backgroundColor = panelComponent.getBackgroundColor();

        if (timer.isExpired())
        {
            switch (plugin.getConfig().expirationEffect())
            {
                case RAVE_TEXT:
                    timeColor = randomColor();
                    nameColor = timeColor;
                    break;

                case FLASH_TEXT_FAST:
                    if ((timer.getFlashCounter() / 10) % 2 == 0)
                        timeColor = nameColor = Color.RED;
                    break;

                case FLASH_TEXT_SLOW:
                    if ((timer.getFlashCounter() / 30) % 2 == 0)
                        timeColor = nameColor = Color.RED;
                    break;

                case RAVE_TIMER:
                    timeColor = nameColor = randomColor();
                    backgroundColor = randomColor();
                    break;

                case FLASH_TIMER:
                    if ((timer.getFlashCounter() / 10) % 2 == 0)
                        backgroundColor = Color.RED;
                    else
                        backgroundColor = new Color(38, 38, 38); // default gray
                    break;

                default:
                    break;
            }
        }

        panelComponent.setBackgroundColor(backgroundColor);
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(LineComponent.builder()
                .left(timer.getName())
                .leftColor(nameColor)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left(timeString)
                .leftColor(timeColor)
                .build());

        return panelComponent.render(graphics);
    }

    private Color randomColor()
    {
        return new Color(
                (int)(Math.random() * 255),
                (int)(Math.random() * 255),
                (int)(Math.random() * 255)
        );
    }

    public VisualTimer getTimer()
    {
        return timer;
    }
}