package de.feelix.sierra.manager.storage;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import de.feelix.sierra.Sierra;
import de.feelix.sierra.check.CheckManager;
import de.feelix.sierra.manager.storage.processor.BrandProcessor;
import de.feelix.sierra.manager.storage.processor.GameModeProcessor;
import lombok.Getter;
import lombok.Setter;
import de.feelix.sierraapi.check.CheckRepository;
import de.feelix.sierraapi.user.impl.SierraUser;
import de.feelix.sierraapi.violation.PunishType;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * PlayerData is a class representing the data associated with a player.
 */
@Getter
@Setter
public class PlayerData implements SierraUser {

    private       User        user;
    private final Set<String> channels = new HashSet<>();

    public int lastBookEditTick;
    public int lastDropItemTick;
    public int lastCraftRequestTick;
    public int dropCount;
    public int recursionCount;
    public int bytesSent;
    public int openWindowType;
    public int openWindowContainer;

    public double packetCount;
    public double packetAllowance = 1000;

    public long joinTime = System.currentTimeMillis();

    public  boolean blocked       = false;
    public  boolean receiveAlerts = false;
    private boolean exempt        = false;
    private boolean hasBrand      = false;

    private String brand = "vanilla";

    public        ClientVersion     clientVersion;
    public        GameMode          gameMode;
    public final  CheckManager      checkManager      = new CheckManager(this);
    private final BrandProcessor    brandProcessor    = new BrandProcessor(this);
    private final GameModeProcessor gameModeProcessor = new GameModeProcessor(this);

    /**
     * The PlayerData function is a constructor that takes in a User object and sets the user variable to it.
     *
     * @param user user Set the user field in this class
     */
    public PlayerData(User user) {
        this.user = user;
        this.clientVersion = user.getClientVersion();
    }

    /**
     * Retrieves the username associated with the PlayerData object.
     *
     * @return the username as a String
     */
    @Override
    public String username() {
        return user.getName();
    }

    /**
     * Retrieves the entity ID associated with the player.
     *
     * @return the entity ID as an integer
     */
    @Override
    public int entityId() {
        return user.getEntityId();
    }

    /**
     * Returns the UUID of the player.
     *
     * @return the UUID of the player
     */
    @Override
    public UUID uuid() {
        return user.getUUID();
    }

    /**
     * Retrieves the timestamp representing the existence of the object since its creation.
     *
     * @return the timestamp representing the existence of the object since its creation
     */
    @Override
    public long existSince() {
        return System.currentTimeMillis() - joinTime;
    }

    /**
     * Retrieves the version of the client used by the player.
     *
     * @return the version of the client as a String
     */
    @Override
    public String version() {
        return user.getClientVersion().name();
    }

    /**
     * The kick method is used to kick the user associated with the PlayerData object.
     * If the user is not null, it will close the user's connection and return true.
     * If the user is null, it will return false.
     *
     * @return true if the user is successfully kicked, false otherwise
     */
    @Override
    public boolean kick() {
        if (user != null) {
            user.closeConnection();
            return true;
        }
        return false;
    }

    /**
     * Returns whether the player is exempt from certain actions or checks.
     *
     * @return true if the player is exempt, false otherwise
     */
    @Override
    public boolean isExempt() {
        return this.exempt;
    }

    /**
     * Sets whether the player is exempt from certain actions or checks.
     *
     * @param b {@code true} if the player should be exempt, {@code false} otherwise
     * @return the updated value of the exempt field
     */
    @Override
    public boolean setExempt(boolean b) {
        this.exempt = b;
        return this.exempt;
    }

    /**
     * Returns whether the player should receive alerts.
     *
     * @return {@code true} if the player should receive alerts, {@code false} otherwise
     */
    @Override
    public boolean isAlerts() {
        return this.receiveAlerts;
    }

    /**
     * Sets whether the player should receive alerts.
     *
     * @param b true to enable alerts, false to disable alerts
     * @return the updated value of receiveAlerts
     */
    @Override
    public boolean setAlerts(boolean b) {
        this.receiveAlerts = b;
        return this.receiveAlerts;
    }

    /**
     * The punish function is used to punish a player for crashing.
     *
     * @param punishType punishType Determine what type of punishment the player should receive
     */
    public void punish(PunishType punishType) {
        setBlocked(true);
        if (punishType == PunishType.BAN && Sierra.getPlugin().getPunishmentConfig().isBan()) {
            ban();
        }
        kick();
    }

    /**
     * The ban method is used to ban a player.
     * It retrieves the punish command from the sierra.yml configuration file and replaces the {username} placeholder with the player's name.
     * Then, it dispatches the command to the console sender.
     */
    private void ban() {
        Bukkit.getScheduler()
            .runTask(
                Sierra.getPlugin(), () -> Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    Sierra.getPlugin()
                        .getSierraConfigEngine()
                        .config()
                        .getString("punish-command", "ban {username} Crashing")
                        .replace("{username}", this.user.getName())
                ));
    }

    /**
     * Retrieves the check repository associated with this instance.
     *
     * @return The check repository associated with this instance.
     */
    @Override
    public CheckRepository checkRepository() {
        return this.checkManager;
    }

}
