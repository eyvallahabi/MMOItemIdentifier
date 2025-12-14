package io.github.shiryu.identifier.menu;

import com.cryptomorin.xseries.XSound;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import io.github.shiryu.identifier.IdentifierPlugin;
import io.github.shiryu.identifier.item.UnidentifiedItem;
import io.github.shiryu.spider.api.config.Configs;
import io.github.shiryu.spider.util.Colored;
import io.github.shiryu.spider.util.ItemBuilder;
import lombok.Getter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@Getter
public class IdentifyMenu implements InventoryHolder {

    private final Player player;
    private Inventory inventory;

    private boolean animating = false;

    public IdentifyMenu(@NotNull final Player player){
        this.player = player;
        this.inventory = create();
        this.inventory.setItem(
                1,
                Configs.create(IdentifierPlugin.getInstance(), "config").get("menu.items.roll", ItemStack.class)
        );
    }

    @NotNull
    public Inventory create(){
        if (this.inventory == null)
            this.inventory = Bukkit.createInventory(this, InventoryType.BLAST_FURNACE, Component.text(Colored.convert(IdentifierPlugin.getInstance().getConfig().getString("menu.title"))));

        return this.inventory;
    }

    public void show(@NotNull final Player player){
        player.openInventory(this.getInventory());
    }

    public void startAnimation(){
        this.animating = true;

        final ItemStack item = inventory.getItem(0);

        if (item == null){
            this.animating = false;
            return;
        }

        final ReadableNBT nbt = NBT.readNbt(item);

        if (!nbt.hasTag("unidentified_item")){
            this.animating = false;
            return;
        }

        final Type type = MMOItems.plugin.getTypes().get(nbt.getString("unidentified_type"));

        if (type == null)
            return;

        final UnidentifiedItem unidentified = IdentifierPlugin.getInstance().getUnidentified(type);

        if (unidentified == null)
            return;

        new BukkitRunnable(){
            private int ticks = 0;
            @Override
            public void run() {
                if (this.ticks >= IdentifierPlugin.getInstance().getConfig().getInt("menu.animation-ticks")){
                    final ItemStack winner = inventory.getItem(0);

                    if (winner != null)
                        prize(winner);

                    animating = false;
                    this.cancel();

                    return;
                }

                final ItemStack item = inventory.getItem(0);

                if (item == null){
                    animating = false;
                    this.cancel();
                    return;
                }

                final XSound sound = XSound.of(IdentifierPlugin.getInstance().getConfig().getString("menu.roll-sound", "BLOCK_NOTE_BLOCK_PLING"))
                        .orElse(null);

                if (sound != null && sound.get() != null){
                    player.getWorld().playSound(
                            player.getLocation(),
                            sound.get(),
                            1.0f,
                            1.0f
                    );
                }

                final ItemStack random = unidentified.identify();

                if (random != null){
                    inventory.setItem(
                            0,
                            random
                    );
                }

                this.ticks++;
            }
        }.runTaskTimer(IdentifierPlugin.getInstance(), 0L, 2L);
    }

    public void prize(@NotNull final ItemStack item){
        this.inventory.setItem(0, ItemBuilder.empty().build());
        this.inventory.setItem(2, item);

        final XSound sound = XSound.of(IdentifierPlugin.getInstance().getConfig().getString("menu.win-sound", "UI_TOAST_CHALLENGE_COMPLETE"))
                .orElse(null);

        if (sound != null && sound.get() != null){
            player.getWorld().playSound(
                    player.getLocation(),
                    sound.get(),
                    1.0f,
                    1.0f
            );
        }
    }

    public void accept(@NotNull final InventoryClickEvent event){
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (this.animating){
            event.setCancelled(true);
            return;
        }

        final int slot = event.getSlot();

        switch (slot){
            case 1 ->{
                final ItemStack item = this.inventory.getItem(0);

                if (item == null){
                    event.setCancelled(true);
                    player.sendMessage(Colored.convert("&cYou need to place an unidentified item in the first slot."));
                    return;
                }

                final ReadableNBT nbt = NBT.readNbt(item);

                if (!nbt.hasTag("unidentified_item")){
                    event.setCancelled(true);
                    player.sendMessage(Colored.convert("&cThe item in the first slot is not an unidentified item."));
                    return;
                }

                event.setCancelled(true);

                startAnimation();
            }
            case 2 ->{
                if (this.animating){
                    event.setCancelled(true);
                    player.sendMessage(Colored.convert("&cYou cannot take the identified item while the identification is in progress."));
                    return;
                }

                final ItemStack prize = this.inventory.getItem(2);

                if (prize == null){
                    event.setCancelled(true);
                    player.sendMessage(Colored.convert("&cThere is no identified item to take."));
                    return;
                }

                player.getInventory().addItem(prize);
                this.inventory.setItem(2, ItemBuilder.empty().build());
            }
        }

    }

}
