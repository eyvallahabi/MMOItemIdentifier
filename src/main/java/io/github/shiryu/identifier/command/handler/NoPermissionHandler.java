package io.github.shiryu.identifier.command.handler;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import dev.rollczi.litecommands.permission.MissingPermissionsHandler;
import io.github.shiryu.spider.util.Colored;
import org.bukkit.command.CommandSender;

public class NoPermissionHandler implements MissingPermissionsHandler<CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions, ResultHandlerChain<CommandSender> chain) {
        final CommandSender sender = invocation.sender();

        sender.sendMessage(Colored.convert("&cYou do not have permission to execute this command."));
    }

}
