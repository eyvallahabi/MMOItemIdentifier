package io.github.shiryu.identifier.command;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import io.github.shiryu.identifier.IdentifierPlugin;
import io.github.shiryu.identifier.item.UnidentifiedItem;
import io.github.shiryu.identifier.menu.IdentifyMenu;
import io.github.shiryu.spider.util.ItemBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Command(name = "mmoitemidentifier")
public class IdentifierCommand {

    @Execute
    public void menu(@Context final Player player){
        new IdentifyMenu(player).show(player);
    }

    @Execute(name = "give")
    @Permission("mmoitemidentifier.command")
    public void give(@Context final CommandSender sender, @Arg final Player target, @Arg final int amount, @Arg final Type type){
        final UnidentifiedItem item = IdentifierPlugin.getInstance().getRandomUnidentifiedOfType(type);

        if (item == null){
            sender.sendMessage("§cNo unidentified items of type " + type.getId() + " found.");
            return;
        }

        final ItemStack give = ItemBuilder.from(item.getItem()).amount(amount).build();

        target.getInventory().addItem(give);

        sender.sendMessage("§aGave " + amount + " unidentified " + type.getId() + " to " + target.getName() + ".");
        target.sendMessage("§aYou have received " + amount + " unidentified " + type.getId() + ".");
    }

    @Execute(name = "identify")
    @Permission("mmoitemidentifier.command")
    public void identify(@Context final Player player){
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().isAir()){
            player.sendMessage("§cYou must hold an unidentified item in your hand to identify it.");
            return;
        }

        final ReadableNBT nbt = NBT.readNbt(hand);

        if (!nbt.hasTag("unidentified_item"))
            return;

        final Type type = MMOItems.plugin.getTypes().get(nbt.getString("unidentified_type"));

        if (type == null)
            return;

        final UnidentifiedItem item = IdentifierPlugin.getInstance().getUnidentified(type);

        if (item == null)
            return;

        if (hand.getAmount() > 1){
            hand.setAmount(hand.getAmount() - 1);
        }else{
            player.getInventory().setItemInMainHand(null);
        }

        final ItemStack identified = item.identify();

        if (identified == null){
            player.sendMessage("§cFailed to identify the item.");
            return;
        }

        player.getInventory().addItem(identified);
        player.sendMessage("§aYou have successfully identified the item!");
    }
}
