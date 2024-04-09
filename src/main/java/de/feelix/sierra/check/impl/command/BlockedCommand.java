package de.feelix.sierra.check.impl.command;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateCommandBlock;
import de.feelix.sierra.Sierra;
import de.feelix.sierra.check.SierraDetection;
import de.feelix.sierra.check.violation.ViolationDocument;
import de.feelix.sierra.manager.packet.IngoingProcessor;
import de.feelix.sierra.manager.storage.PlayerData;
import de.feelix.sierraapi.check.SierraCheckData;
import de.feelix.sierraapi.check.CheckType;
import de.feelix.sierraapi.violation.PunishType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SierraCheckData(checkType = CheckType.COMMAND)
public class BlockedCommand extends SierraDetection implements IngoingProcessor {

    private static final Pattern PLUGIN_EXCLUSION = Pattern.compile("/(\\S+:)");

    private final List<String> disallowedCommands = Sierra.getPlugin()
        .getSierraConfigEngine().config().getStringList("disallowed-commands");

    public BlockedCommand(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void handle(PacketReceiveEvent event, PlayerData playerData) {

        if (!Sierra.getPlugin()
            .getSierraConfigEngine()
            .config()
            .getBoolean("block-disallowed-commands", true)) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.UPDATE_COMMAND_BLOCK) {

            WrapperPlayClientUpdateCommandBlock wrapper = new WrapperPlayClientUpdateCommandBlock(event);

            String string = wrapper.getCommand().toLowerCase().replaceAll("\\s+", " ");

            for (String disallowedCommand : disallowedCommands) {
                if (string.contains(disallowedCommand)) {
                    if (playerHasPermission(event)) {
                        violation(event, ViolationDocument.builder()
                            .debugInformation(string)
                            .punishType(PunishType.MITIGATE)
                            .build());
                    }
                }
            }

        } else if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
            String                       message = wrapper.getMessage().toLowerCase().replaceAll("\\s+", " ");
            checkForDoubleCommands(event, message);
        } else if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {
            WrapperPlayClientChatCommand wrapper = new WrapperPlayClientChatCommand(event);
            String message = wrapper.getCommand().toLowerCase().replaceAll("\\s+", " ");
            checkForDoubleCommands(event, message);
        }
    }

    private void checkForDoubleCommands(PacketReceiveEvent event, String message) {
        for (String disallowedCommand : disallowedCommands) {
            if (message.contains(disallowedCommand)) {
                if (playerHasPermission(event)) {
                    violation(event, ViolationDocument.builder()
                        .debugInformation(message)
                        .punishType(PunishType.MITIGATE)
                        .build());
                }
            }
            String pluginCommand = replaceGroup(PLUGIN_EXCLUSION.pattern(), message);
            if (pluginCommand.contains(disallowedCommand)) {
                if (playerHasPermission(event)) {
                    violation(event, ViolationDocument.builder()
                        .debugInformation(pluginCommand)
                        .punishType(PunishType.MITIGATE)
                        .build());
                }
            }
        }
    }

    private boolean playerHasPermission(PacketReceiveEvent event) {
        Player player = Bukkit.getPlayer(event.getUser().getName());

        if (player == null) {
            return true;
        }

        return !player.isOp();
    }

    private String replaceGroup(String regex, String source) {

        Matcher m = Pattern.compile(regex).matcher(source);
        for (int i = 0; i < 1; i++)
            if (!m.find()) return source; // pattern not met, may also throw an exception here
        return new StringBuilder(source).replace(m.start(1), m.end(1), "").toString();
    }
}


