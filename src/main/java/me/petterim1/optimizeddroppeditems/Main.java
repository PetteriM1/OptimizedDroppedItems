package me.petterim1.optimizeddroppeditems;

import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.PluginBase;

public class Main extends PluginBase {

    @Override
    public void onEnable() {
        Entity.registerEntity("Item", OptimizedDroppedItem.class);
        getLogger().info("§aOptimized dropped item entity registered");
        Entity.registerEntity("XpOrb", OptimizedXPOrb.class);
        getLogger().info("§aOptimized xp orb entity registered");
    }
}
