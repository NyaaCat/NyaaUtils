package cat.nyaa.utils;


import cat.nyaa.nyaautils.NyaaUtils;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtil {
    private final NyaaUtils plugin;
    public Economy eco = null;
    public Chat chat = null;
    public boolean PEX = false;

    public VaultUtil(NyaaUtils p) {
        plugin = p;
        RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            eco = provider.getProvider();
        } else {
            throw new RuntimeException("Vault Error: No EconomyProvider found");
        }
        RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        chat = chatProvider.getProvider();
        if (plugin.getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
            PEX = true;
        }
    }

    public double balance(OfflinePlayer p) {
        return eco.getBalance(p);
    }

    public boolean enoughMoney(OfflinePlayer p, long money) {
        return money <= balance(p);
    }

    public boolean enoughMoney(OfflinePlayer p, double money) {
        return money <= balance(p);
    }

    public boolean withdraw(OfflinePlayer p, long money) {
        EconomyResponse rsp = eco.withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean withdraw(OfflinePlayer p, double money) {
        EconomyResponse rsp = eco.withdrawPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer p, long money) {
        EconomyResponse rsp = eco.depositPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer p, double money) {
        EconomyResponse rsp = eco.depositPlayer(p, money);
        return rsp.transactionSuccess();
    }

    public String getPlayerPrefix(Player player) {
        return chat.getPlayerPrefix(player);
    }

    public void setPlayerPrefix(Player player, String prefix) {
        if (PEX) {
            chat.setPlayerPrefix(null, player, prefix);
        } else {
            chat.setPlayerPrefix(player, prefix);
        }
    }

    public String getPlayerSuffix(Player player) {
        return chat.getPlayerSuffix(player);
    }

    public void setPlayerSuffix(Player player, String suffix) {
        if (PEX) {
            chat.setPlayerSuffix(null, player, suffix);
        } else {
            chat.setPlayerSuffix(player, suffix);
        }
    }
}
