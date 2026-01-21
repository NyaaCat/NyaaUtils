package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.commandwarpper.EsschatCmdWarpper;
import cat.nyaa.nyaautils.dropprotect.DropProtectListener;
import cat.nyaa.nyaautils.elytra.ElytraEnhanceListener;
import cat.nyaa.nyaautils.elytra.FuelManager;
import cat.nyaa.nyaautils.extrabackpack.ExtraBackpackGUI;
import cat.nyaa.nyaautils.extrabackpack.ExtraBackpackListener;
import cat.nyaa.nyaautils.lootprotect.LootProtectListener;
import cat.nyaa.nyaautils.mention.MentionListener;
import cat.nyaa.nyaautils.realm.RealmListener;
import cat.nyaa.nyaautils.vote.VoteTask;
import com.earth2me.essentials.ISettings;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.ess3.api.IEssentials;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public class NyaaUtils extends JavaPlugin {
    public static NyaaUtils instance;
    public I18n i18n;
    public CommandHandler commandHandler;
    public Configuration cfg;
    public LootProtectListener lpListener;
    public DropProtectListener dpListener;
    public DamageStatListener dsListener;
    public ElytraEnhanceListener elytraEnhanceListener;
    public FuelManager fuelManager;
    public WorldEditPlugin worldEditPlugin;
    public RealmListener realmListener;
    public IEssentials ess;
    public MentionListener mentionListener;
    public EsschatCmdWarpper esschatCmdWarpper;
    public VoteTask voteTask;
    public ExtraBackpackListener extraBackpackListener;

    @Override
    public void onEnable() {
        instance = this;
        cfg = new Configuration(this);
        cfg.load();
        i18n = new I18n(this, cfg.language);
        commandHandler = new CommandHandler(this, i18n);
        getCommand("nyaautils").setExecutor(commandHandler);
        getCommand("nyaautils").setTabCompleter((TabCompleter) commandHandler);
        lpListener = new LootProtectListener(this);
        dpListener = new DropProtectListener(this);
        dsListener = new DamageStatListener(this);
        elytraEnhanceListener = new ElytraEnhanceListener(this);
        fuelManager = new FuelManager(this);
        if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }
        realmListener = new RealmListener(this);
        ess = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
        mentionListener = new MentionListener(this);
        extraBackpackListener = new ExtraBackpackListener(this);
        try {
            ISettings settings = ess.getSettings();
            Class<? extends ISettings> essSettingsClass = settings.getClass();
            Long timeout = (Long) essSettingsClass.getMethod("getLastMessageReplyRecipientTimeout").invoke(settings);
            // TODO: sort out reply recipient when isLastMessageReplyRecipient set to false instead disabling it
            Boolean allow = (Boolean) essSettingsClass.getMethod("isLastMessageReplyRecipient").invoke(settings);
            esschatCmdWarpper = new EsschatCmdWarpper(this, allow, timeout);
        } catch (NoSuchMethodException e) {
            getLogger().warning("EssentialsX not available, not enabling mention notify in /reply commands");
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            getLogger().warning("Unexpected error when enabling mention notify in EssentialsX commands");
        }

        voteTask = null;
    }

    @Override
    public void onDisable() {
        ExtraBackpackGUI.closeAll();
        getServer().getScheduler().cancelTasks(this);
        getCommand("nyaautils").setExecutor(null);
        getCommand("nyaautils").setTabCompleter(null);
        HandlerList.unregisterAll(this);
        cfg.save();
    }
}
