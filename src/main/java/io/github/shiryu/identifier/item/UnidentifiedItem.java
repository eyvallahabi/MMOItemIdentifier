package io.github.shiryu.identifier.item;

import io.github.shiryu.spider.api.config.Config;
import io.github.shiryu.spider.api.config.Configs;
import io.github.shiryu.spider.api.config.impl.Section;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class UnidentifiedItem {

    private final Type type;
    private final ItemStack item;

    @Nullable
    public ItemStack identify(){
        final Collection<MMOItemTemplate> templates = MMOItems.plugin.getTemplates()
                .getTemplates(this.type);

        if(templates.isEmpty())
            return null;

        final ItemTier tier = randomTier();
        final int level = randomLevel(tier, 1);

        final MMOItemTemplate template = templates.stream().findAny().orElse(null);

        if (template == null)
            return null;

        return template.newBuilder(level, tier)
                .build()
                .newBuilder()
                .build();
    }

    @NotNull
    public ItemTier randomTier() {
        final Random random = new Random();

        final Config config = Configs.create(MMOItems.plugin, "item-tiers");

        Map<ItemTier, Double> chances = new LinkedHashMap<>();
        double totalChance = 0;

        for (ItemTier tier : MMOItems.plugin.getTiers().getAll()) {
            final String id = tier.getId();
            final Section section = config.getSection(id);

            if (section == null)
                continue;

            double chance = section.getOrSet("generation.chance", 0.0);

            if (chance <= 0)
                continue;

            chances.put(tier, chance);
            totalChance += chance;
        }

        if (chances.isEmpty())
            return MMOItems.plugin.getTiers().get("COMMON");

        double roll = random.nextDouble() * totalChance;
        double current = 0;

        for (Map.Entry<ItemTier, Double> entry : chances.entrySet()) {
            current += entry.getValue();
            if (roll <= current)
                return entry.getKey();
        }

        return chances.keySet().iterator().next();
    }

    public int randomLevel(ItemTier tier, int baseLevel) {
        final Random random = new Random();

        final Config config = Configs.create(MMOItems.plugin, "item-tiers");

        int range = config.getOrSet(tier.getId() + ".unidentification.range", 0);

        if (range <= 0)
            return baseLevel;

        int min = Math.max(1, baseLevel - range);
        int max = baseLevel + range;

        return random.nextInt(max - min + 1) + min;
    }
}
