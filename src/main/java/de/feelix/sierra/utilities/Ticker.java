package de.feelix.sierra.utilities;

import de.feelix.sierra.Sierra;
import de.feelix.sierra.manager.storage.SierraDataManager;
import de.feelix.sierra.manager.storage.PlayerData;
import de.feelix.sierraapi.check.CheckType;
import de.feelix.sierraapi.check.impl.SierraCheck;
import io.github.retrooper.packetevents.util.FoliaCompatUtil;
import lombok.Getter;

/**
 * The {@code Ticker} class represents a timer that runs asynchronously and performs tasks at regular intervals.
 * {@code Ticker} is a singleton and can be accessed using the {@code instance} variable.
 * It maintains a {@code currentTick} count that increments by 1 every tick.
 * It also has a {@code task} that runs every second and performs certain actions.
 */
@Getter
public class Ticker {

    /**
     * The {@code Ticker} class represents a timer that runs asynchronously and performs tasks at regular intervals.
     * {@code Ticker} is a singleton and can be accessed using the {@code instance} variable.
     * It maintains a {@code currentTick} count that increments by 1 every tick.
     * It also has a {@code task} that runs every second and performs certain actions.
     *
     * <p>
     * The {@code Ticker} class is part of the {@code Sierra} plugin and can be obtained through the {@code Sierra}
     * class.
     * </p>
     *
     * @see Sierra#getPlugin()
     * @see Ticker#currentTick
     */
    @Getter
    private static Ticker instance;

    /**
     * Represents the current tick count of the ticker.
     * The tick count increments by 1 every tick.
     *
     * @see Ticker
     */
    private int currentTick;

    /**
     * Ticker class represents a timer that runs asynchronously and performs tasks at regular intervals.
     */
    public Ticker() {
        instance = this;
        FoliaCompatUtil.runTaskTimerAsync(Sierra.getPlugin(), o -> currentTick++, 1, 1);

        FoliaCompatUtil.runTaskTimerAsync(Sierra.getPlugin(), o -> {
            double maxPacketsPerSecond = 1000;
            double maxPacketAllowance  = maxPacketsPerSecond * 2;

            for (PlayerData value : SierraDataManager.getInstance().getPlayerData().values()) {
                value.setPacketAllowance(maxPacketAllowance);
                value.setPacketCount(0);
                value.setBytesSent(0);

                for (SierraCheck sierraCheck : value.getCheckManager().availableChecks()) {
                    if (sierraCheck.checkType() == CheckType.LATENCY_ABUSE) {
                        if (sierraCheck.violations() > 0) {
                            sierraCheck.setViolations(sierraCheck.violations() - 1);
                        }
                    }
                }
            }
        }, 0, 20);
    }
}
