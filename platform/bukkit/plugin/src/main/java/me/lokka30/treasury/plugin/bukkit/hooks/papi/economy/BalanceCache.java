/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/lokka30/Treasury>.
 */

package me.lokka30.treasury.plugin.bukkit.hooks.papi.economy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.account.PlayerAccount;
import me.lokka30.treasury.api.economy.currency.Currency;
import me.lokka30.treasury.plugin.bukkit.TreasuryBukkit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public class BalanceCache extends BukkitRunnable {

    private final Multimap<UUID, Map.Entry<String, BigDecimal>> balances = HashMultimap.create();
    private final int delay;
    private final AtomicReference<EconomyProvider> providerRef;

    public BalanceCache(int delay, AtomicReference<EconomyProvider> providerRef) {
        this.delay = delay;
        this.providerRef = providerRef;
    }

    public void start(TreasuryBukkit plugin) {
        runTaskTimerAsynchronously(plugin, 20, delay * 20L);
    }

    public @Nullable BigDecimal getBalance(UUID uuid, String currencyId) {
        Collection<Map.Entry<String, BigDecimal>> collection = balances.get(uuid);
        // optimise
        if (collection.isEmpty()) {
            return null;
        }
        return collection
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(currencyId))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    @Override
    public void run() {
        EconomyProvider provider = providerRef.get();
        if (provider == null) {
            return;
        }
        balances.clear();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            PlayerAccount account = provider
                    .hasPlayerAccount(player.getUniqueId())
                    .thenCompose(val -> {
                        if (val) {
                            return provider.retrievePlayerAccount(player.getUniqueId());
                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    })
                    .join();
            if (account == null) {
                continue;
            }
            for (Currency currency : provider.getCurrencies()) {
                BigDecimal balance = account.retrieveBalance(currency).join();
                if (balance == null || balance.equals(BigDecimal.ZERO)) {
                    continue;
                }
                balances.put(
                        player.getUniqueId(),
                        new AbstractMap.SimpleEntry<>(currency.getIdentifier(), balance)
                );
            }
        }
    }

}
