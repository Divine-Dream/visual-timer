package com.visualtimer;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.awt.*;
import java.awt.Dimension;
import java.awt.Graphics2D;

@Slf4j
public class VisualTimerScreenOverlay extends Overlay
{
    private final VisualTimerPlugin plugin;

    @Inject
    public VisualTimerScreenOverlay(VisualTimerPlugin plugin)
    {
        this.plugin = plugin;
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isScreenFlashing() || !plugin.isScreenFlashVisible())
        {
            return null;
        }

        // Light red color with ~35% opacity
        Color flashColor = new Color(255, 0, 0, 90);
        graphics.setColor(flashColor);

        Dimension dim = plugin.getClient().getRealDimensions();
        log.debug("Overlay dimensions: width={}, height={}", dim.width, dim.height);
        int width = dim.width;
        int height = dim.height;

        log.debug("Rendering screen flash overlay");
        graphics.fillRect(0, 0, width, height);

        return new Dimension(width, height);
    }
}