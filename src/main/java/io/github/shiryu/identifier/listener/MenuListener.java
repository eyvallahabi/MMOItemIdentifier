package io.github.shiryu.identifier.listener;

import io.github.shiryu.identifier.menu.IdentifyMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {

    @EventHandler
    public void click(final InventoryClickEvent event){
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        final Inventory inventory = event.getInventory();

        if (inventory.getHolder() == null)
            return;

        if (!(inventory.getHolder() instanceof IdentifyMenu menu))
            return;

        menu.accept(event);
    }
}
