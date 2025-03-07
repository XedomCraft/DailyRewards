package cz.revivalo.dailyrewards;

import com.tchristofferson.configupdater.ConfigUpdater;
import cz.revivalo.dailyrewards.commands.RewardCommand;
import cz.revivalo.dailyrewards.guimanager.InventoryClickListener;
import cz.revivalo.dailyrewards.guimanager.GuiManager;
import cz.revivalo.dailyrewards.files.Lang;
import cz.revivalo.dailyrewards.files.PlayerData;
import cz.revivalo.dailyrewards.rewardmanager.Cooldowns;
import cz.revivalo.dailyrewards.rewardmanager.JoinNotification;
import cz.revivalo.dailyrewards.rewardmanager.Placeholder;
import cz.revivalo.dailyrewards.rewardmanager.RewardManager;
import cz.revivalo.dailyrewards.updatechecker.Notification;
import cz.revivalo.dailyrewards.updatechecker.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

public final class DailyRewards extends JavaPlugin {

    private DailyRewards plugin;
    private Cooldowns cooldowns;
    private RewardManager rewardManager;
    private GuiManager guiManager;
    public static boolean PAPI = false;
    public static boolean newestVersion;
    public static boolean isHexSupport;

    //
    // TODO:
    // MySQL support
    // Auto claim reward
    //

    @Override
    public void onEnable() {
        plugin = this;

        isHexSupport = Bukkit.getBukkitVersion().contains("6") || Bukkit.getBukkitVersion().contains("7") || Bukkit.getBukkitVersion().contains("8") || Bukkit.getBukkitVersion().contains("9");

        int pluginId = 12070;
        new Metrics(this, pluginId);

        Logger logger = this.getLogger();

        new UpdateChecker(this, 81780).getVersion(version -> {
            if (Lang.UPDATE_CHECKER.getBoolean()) {
                String actualVersion = this.getDescription().getVersion();
                if (actualVersion.equalsIgnoreCase(version)) {
                    logger.info("You are running latest release (" + version + ")");
                    newestVersion = true;
                } else {
                    logger.info("There is a new v" + version + " update available (You are running v" + actualVersion + ")." + "\n" + "Outdated versions are no longer supported, get new one here https://www.spigotmc.org/resources/%E2%9A%A1-daily-weekly-monthly-rewards-papi-support-1-13-1-17.81780/");
                    newestVersion = false;
                }
            }
        });

        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            ConfigUpdater.update(plugin, "config.yml", configFile, Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadConfig();
        cooldowns = new Cooldowns();
        rewardManager = new RewardManager(plugin);
        guiManager = new GuiManager(plugin);
        registerCommands();
        implementsListeners();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PAPI = true;
            new Placeholder(this).register();
        } else {
            logger.warning("Could not find PlaceholderAPI, placeholders will not work");
        }
    }

    @Override
    public void onDisable() {
        PlayerData.removeConfigs();
    }

    void registerCommands(){
        Objects.requireNonNull(getCommand("reward")).setExecutor(new RewardCommand(plugin));
        Objects.requireNonNull(getCommand("rewards")).setExecutor(new RewardCommand(plugin));
    }

    void implementsListeners(){
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InventoryClickListener(rewardManager), plugin);
        pm.registerEvents(new JoinNotification(plugin), plugin);
        pm.registerEvents(new Notification(), plugin);
    }

    public String getPremium(final Player player, final String type){
        if (player.hasPermission("dailyreward." + type + ".premium")){
            return "_PREMIUM";
        } else {
            return "";
        }
    }

    public Cooldowns getCooldowns() {return cooldowns;}

    public RewardManager getRewardManager() {return rewardManager;}

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
