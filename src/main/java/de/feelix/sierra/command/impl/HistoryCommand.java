package de.feelix.sierra.command.impl;


import de.feelix.sierra.Sierra;
import de.feelix.sierra.manager.storage.history.HistoryDocument;
import de.feelix.sierra.utilities.FormatUtils;
import de.feelix.sierra.utilities.pagination.Pagination;
import de.feelix.sierraapi.commands.*;
import de.feelix.sierraapi.history.History;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistoryCommand implements ISierraCommand {

    /**
     * Process method to show the action history.
     *
     * @param sierraSender    The sender object.
     * @param abstractCommand The abstract command object.
     * @param sierraLabel     The sierra label object.
     * @param sierraArguments The sierra arguments object.
     */
    @Override
    public void process(ISierraSender sierraSender, IBukkitAbstractCommand abstractCommand, ISierraLabel sierraLabel,
                        ISierraArguments sierraArguments) {

        if (!validateArguments(sierraArguments)) {
            sendHelpSyntax(sierraSender);
            return;
        }

        Pagination<History> pagination = setupPagination();
        int page = correctPage(
            FormatUtils.toInt(sierraArguments.getArguments().get(1)), pagination.totalPages());
        sendMessage(sierraSender, page, pagination);

        List<History> historyDocumentList = pagination.itemsForPage(page);

        if (historyDocumentList.isEmpty()) {
            sierraSender.getSender().sendMessage(Sierra.PREFIX + " §cNo history available");
            return;
        }

        sendHistoryMessages(sierraSender, historyDocumentList);
    }

    /**
     * Sends the history messages to the specified sender.
     *
     * @param sierraSender        The ISierraSender object representing the sender.
     * @param historyDocumentList The list of HistoryDocument objects containing the history information.
     */
    private void sendHistoryMessages(ISierraSender sierraSender, List<History> historyDocumentList) {
        for (History historyDocument : historyDocumentList) {
            sierraSender.getSenderAsPlayer()
                .spigot()
                .sendMessage(createHistoryMessage((HistoryDocument) historyDocument));
        }
    }

    /**
     * Validates the arguments passed with a command.
     *
     * @param sierraArguments The ISierraArguments object representing the arguments.
     * @return true if the number of arguments is greater than 1, false otherwise.
     */
    private boolean validateArguments(ISierraArguments sierraArguments) {
        return sierraArguments.getArguments().size() > 1;
    }

    /**
     * Sets up pagination for the history documents.
     *
     * @return A Pagination object containing the sorted history documents.
     */
    private Pagination<History> setupPagination() {
        List<History> list = new ArrayList<>(Sierra.getPlugin()
                                                 .getSierraDataManager()
                                                 .getHistories());
        list.sort(Comparator.comparing(History::timestamp).reversed());
        return new Pagination<>(list, 10);
    }

    /**
     * Corrects the page number by ensuring it is within the valid range of pages.
     *
     * @param page       The current page number.
     * @param totalPages The total number of pages.
     * @return The corrected page number.
     */
    private int correctPage(int page, int totalPages) {
        if (page > totalPages || page < 0) {
            return 1;
        }
        return page;
    }

    /**
     * Sends a formatted message to the provided ISierraSender object using the specified pagination details.
     *
     * @param sierraSender - The ISierraSender object representing the sender to send the message to.
     * @param page         - The current page number.
     * @param pagination   - A Pagination object containing the history documents.
     */
    private void sendMessage(ISierraSender sierraSender, int page, Pagination<History> pagination) {
        int    totalHistory = pagination.getItems().size();
        String unformulated = "%s §fShowing entries: §7(page §c%s §7of §c%d §7- §c%d §7entries)";
        sierraSender.getSender()
            .sendMessage(String.format(unformulated, Sierra.PREFIX, page, pagination.totalPages(), totalHistory));
    }

    /**
     * Creates a formatted history message based on the provided HistoryDocument.
     *
     * @param historyDocument The HistoryDocument containing the history information.
     * @return The formatted history message.
     */
    private BaseComponent[] createHistoryMessage(HistoryDocument historyDocument) {
        return FormatUtils.formatColor(String.format(
            "§7[%s] §c%s §7(%dms) -> §c%s §7(%s)",
            historyDocument.formatTimestamp(),
            historyDocument.username(),
            historyDocument.ping(),
            historyDocument.punishType().historyMessage(),
            historyDocument.shortenDescription()
        ));
    }

    /**
     * Sends the help syntax for the command.
     *
     * @param sierraSender The sender object.
     */
    private void sendHelpSyntax(ISierraSender sierraSender) {
        sierraSender.getSender().sendMessage(Sierra.PREFIX + " §cInvalid usage, try /sierra history <page>");
    }

    /**
     * Converts an ID and arguments into a list of strings.
     *
     * @param id   The ID to convert.
     * @param args The arguments to consider while converting.
     * @return The converted list of strings.
     */
    @Override
    public List<String> fromId(int id, String[] args) {
        if (id == 1) {
            return Collections.singletonList("history");
        } else if (id == 2 && args[0].equalsIgnoreCase("history")) {
            return Collections.singletonList("1");
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the description of this method.
     *
     * @return The description of the method
     */
    @Override
    public String description() {
        return "History of player`s punishments";
    }
}
