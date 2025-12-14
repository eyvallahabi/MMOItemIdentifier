package io.github.shiryu.identifier;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBT;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import io.github.shiryu.identifier.command.IdentifierCommand;
import io.github.shiryu.identifier.command.argument.TypeArgument;
import io.github.shiryu.identifier.command.handler.InvalidHandler;
import io.github.shiryu.identifier.command.handler.NoPermissionHandler;
import io.github.shiryu.identifier.item.UnidentifiedItem;
import io.github.shiryu.identifier.listener.MenuListener;
import io.github.shiryu.spider.api.config.Config;
import io.github.shiryu.spider.api.config.Configs;
import io.github.shiryu.spider.api.config.impl.Section;
import io.github.shiryu.spider.util.Colored;
import io.github.shiryu.spider.util.ItemBuilder;
import lombok.Getter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public final class IdentifierPlugin extends JavaPlugin {

    @Getter
    private static IdentifierPlugin instance;

    private Config items;

    private final List<UnidentifiedItem> unidentified = Lists.newArrayList();

    private LiteCommands<CommandSender> lite;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        this.items = Configs.create(this, "items", "", true);

        this.loadItems();

        this.lite = LiteBukkitFactory.builder()
                .argument(Type.class, new TypeArgument())
                .invalidUsage(new InvalidHandler())
                .missingPermission(new NoPermissionHandler())
                .commands(new IdentifierCommand())
                .build();

        this.lite.register();

        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
    }

    @Nullable
    public UnidentifiedItem getUnidentified(@NotNull final Type type){
        return this.unidentified.stream()
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public UnidentifiedItem getRandomUnidentifiedOfType(@NotNull final Type type){
        final List<UnidentifiedItem> filtered = new ArrayList<>();

        for (final UnidentifiedItem item : this.unidentified){
            if (item.getType().equals(type)){
                filtered.add(item);
            }
        }

        if (filtered.isEmpty())
            return null;

        final Random random = new Random();
        return filtered.get(random.nextInt(filtered.size()));
    }

    private void loadItems(){
        this.unidentified.clear();

        final Section section = this.items.getSection("items");

        if (section == null)
            return;

        section.forEach(key ->{
            final Type type = MMOItems.plugin.getTypes().get(section.getOrSet(key + ".type", "SWORD"));
            ItemStack item = section.get(key + ".item", ItemStack.class);

            if (type == null)
                return;

            if (item == null)
                return;

            item = ItemBuilder.from(item)
                    .updateLore(lore ->{
                        List<String> updated = new ArrayList<>(lore);

                        final Map<ItemTier, Double> chances = getTierChancesPercent();

                        for (final Map.Entry<ItemTier, Double> entry : chances.entrySet()){
                            System.out.println("Replacing tier " + entry.getKey().getId() + " with chance " + entry.getValue());
                            if (entry.getValue() <= 1){
                                updated = Colored.replaceAll(updated)
                                        .replaceAll(
                                                "%chance_" + entry.getKey().getId().toLowerCase(Locale.ENGLISH) + "%",
                                                "???"
                                        )
                                        .build();
                            }else{
                                updated = Colored.replaceAll(updated)
                                        .replaceAll(
                                                "%chance_" + entry.getKey().getId().toLowerCase(Locale.ENGLISH) + "%",
                                                String.format("%.2f", entry.getValue()) + "%"
                                        )
                                        .build();
                            }
                        }

                        return updated;
                    })
                    .build();

            NBT.modify(item, nbt ->{
                nbt.setBoolean("unidentified_item", true);
                nbt.setString("unidentified_type", type.getId());
            });


            this.unidentified.add(new UnidentifiedItem(type, item));
        });

        getLogger().info("Loaded " + this.unidentified.size() + " unidentified items.");
    }

    @NotNull
    public Map<ItemTier, Double> getTierChancesPercent() {
        final Config config = Configs.create(MMOItems.plugin, "item-tiers");

        Map<ItemTier, Double> raw = new LinkedHashMap<>();
        double total = 0;

        for (ItemTier tier : MMOItems.plugin.getTiers().getAll()) {
            double chance = config.getOrSet(tier.getId() + ".generation.chance", 0.0);

            if (chance <= 0)
                continue;

            raw.put(tier, chance);
            total += chance;
        }

        final Map<ItemTier, Double> percent = new LinkedHashMap<>();

        for (Map.Entry<ItemTier, Double> entry : raw.entrySet()) {
            percent.put(entry.getKey(), (entry.getValue() / total) * 100.0);
        }

        return percent;
    }

}
