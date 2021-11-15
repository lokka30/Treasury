package me.lokka30.treasury.plugin.core.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.lokka30.treasury.plugin.core.command.subcommand.HelpSubcommand;
import me.lokka30.treasury.plugin.core.command.subcommand.InfoSubcommand;
import me.lokka30.treasury.plugin.core.command.subcommand.ReloadSubcommand;
import me.lokka30.treasury.plugin.core.command.subcommand.migrate.MigrateSubcommand;
import me.lokka30.treasury.plugin.core.config.messaging.Message;
import me.lokka30.treasury.plugin.core.config.messaging.MessageKey;
import me.lokka30.treasury.plugin.core.config.messaging.MessagePlaceholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class, containing the logic of the treasury command.
 *
 * @author MrIvanPlays
 * @since v1.0.0
 */
public final class TreasuryBaseCommand {

    private Map<String, Subcommand> subcommands;

    public TreasuryBaseCommand() {
        this.subcommands = new HashMap<>();
        registerSubcommand("help", new HelpSubcommand());
        registerSubcommand("info", new InfoSubcommand());
        registerSubcommand("reload", new ReloadSubcommand());
        registerSubcommand("migrate", new MigrateSubcommand());
    }

    /**
     * Registers a new subcommand to handle.
     *
     * @param name subcommand name
     * @param subcommand subcommand
     */
    private void registerSubcommand(@NotNull String name, @NotNull Subcommand subcommand) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(subcommand, "subcommand");
        if (subcommands.containsKey(name)) {
            subcommands.replace(name, subcommand);
        } else {
            subcommands.put(name, subcommand);
        }
    }

    /**
     * Executes the base /treasury command.
     *
     * @param sender who ran the command
     * @param label command label
     * @param args command args
     */
    public void execute(@NotNull CommandSource sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(
                    Message.of(MessageKey.INVALID_USAGE_UNSPECIFIED, MessagePlaceholder.placeholder("label", label))
            );
            return;
        }
        Subcommand subcommand = subcommands.get(args[0]);
        if (subcommand == null) {
            sender.sendMessage(
                    Message.of(
                            MessageKey.INVALID_USAGE_SPECIFIED,
                            MessagePlaceholder.placeholder("label", label),
                            MessagePlaceholder.placeholder("subcommand", args[0])
                    )
            );
            return;
        }
        subcommand.execute(
                sender,
                label,
                args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length)
        );
    }

    @NotNull
    private final List<String> subcommandCompletion = Arrays.asList("help", "info", "migrate", "reload");

    /**
     * Runs completions for the base /treasury command.
     *
     * @param sender who asked for tab completions
     * @param args command arguments
     * @return list with completions, can be null
     */
    @Nullable
    public List<String> complete(@NotNull CommandSource sender, @NotNull String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        } else if (args.length == 1) {
            return subcommandCompletion;
        } else {
            Subcommand subcommand = subcommands.get(args[0]);
            if (subcommand == null) {
                return Collections.emptyList();
            }
            return subcommand.complete(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
        }
    }
}