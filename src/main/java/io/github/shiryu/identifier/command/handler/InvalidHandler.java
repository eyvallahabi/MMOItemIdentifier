package io.github.shiryu.identifier.command.handler;

import com.google.common.collect.ImmutableMap;
import com.mysql.cj.Messages;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import io.github.shiryu.spider.util.Colored;
import org.bukkit.command.CommandSender;

public class InvalidHandler implements InvalidUsageHandler<CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        final CommandSender sender = invocation.sender();
        final Schematic schematic = result.getSchematic();

        if (schematic.isOnlyFirst()) {
            sender.sendMessage(Colored.convert("&cInvalid command usage. Correct usage: &e" + schematic.first()));
            return;
        }

        sender.sendMessage(Colored.convert("&cInvalid command usage. Correct usages:"));
    }


}

