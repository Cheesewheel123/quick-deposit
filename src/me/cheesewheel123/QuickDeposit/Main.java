package me.cheesewheel123.QuickDeposit;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new QuickDepositListener(), this);
    }

}
