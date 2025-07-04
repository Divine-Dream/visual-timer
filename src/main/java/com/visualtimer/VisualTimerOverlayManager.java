package com.visualtimer;

import com.google.common.collect.Lists;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.Overlay;

@Singleton
public class VisualTimerOverlayManager
{
    private final OverlayManager overlayManager;
    private final VisualTimerPlugin plugin;

    @Getter
    private final List<VisualTimerOverlay> activeOverlays = Lists.newArrayList();

    @Inject
    public VisualTimerOverlayManager(OverlayManager overlayManager, VisualTimerPlugin plugin)
    {
        this.overlayManager = overlayManager;
        this.plugin = plugin;
    }

    public void add(Overlay overlay)
    {
        overlayManager.add(overlay); // this is the RuneLite OverlayManager
    }
    public void remove(Overlay overlay) { overlayManager.remove(overlay); }


    public void addTimer(VisualTimer timer)
    {
        VisualTimerOverlay overlay = new VisualTimerOverlay(plugin.getClient(), plugin, timer);
        activeOverlays.add(overlay);
        overlayManager.add(overlay);
    }

    public void removeTimer(VisualTimerOverlay overlay)
    {
        activeOverlays.remove(overlay);
        overlayManager.remove(overlay);
    }

    public void removeAllTimers()
    {
        for (VisualTimerOverlay overlay : activeOverlays)
        {
            overlayManager.remove(overlay);
        }
        activeOverlays.clear();
    }
}