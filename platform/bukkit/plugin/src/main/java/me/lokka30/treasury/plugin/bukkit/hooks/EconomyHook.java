/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/lokka30/Treasury>.
 */

package me.lokka30.treasury.plugin.bukkit.hooks;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;
import me.lokka30.treasury.api.common.service.Service;
import me.lokka30.treasury.api.common.service.ServiceRegistry;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.account.PlayerAccount;
import me.lokka30.treasury.api.economy.currency.Currency;
import me.lokka30.treasury.api.economy.response.EconomySubscriber;
import me.lokka30.treasury.plugin.bukkit.TreasuryBukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EconomyHook implements TreasuryPAPIHook {

    private EconomyProvider provider;
    private final DecimalFormat format = new DecimalFormat("#,###");
    private final PAPIExpansion expansion;
    private final TreasuryBukkit plugin;
    private final String k;
    private final String m;
    private final String b;
    private final String t;
    private final String q;
    private Baltop baltop;

    public EconomyHook(@NotNull PAPIExpansion expansion, @NotNull TreasuryBukkit plugin) {
        this.expansion = expansion;
        this.plugin = plugin;
        this.k = expansion.getString("formatting.thousands", "k");
        this.m = expansion.getString("formatting.millions", "M");
        this.b = expansion.getString("formatting.billions", "B");
        this.t = expansion.getString("formatting.trillions", "T");
        this.q = expansion.getString("formatting.quadrillions", "Q");
    }

    @Override
    public String prefix() {
        return "eco_";
    }

    @Override
    public boolean setup() {
        Optional<Service<EconomyProvider>> serviceOpt = ServiceRegistry.INSTANCE.serviceFor(
                EconomyProvider.class);
        if (serviceOpt.isPresent()) {
            provider = serviceOpt.get().get();
            this.baltop = new Baltop(
                    expansion.getBoolean("baltop.enabled", false),
                    expansion.getInt("baltop.cache_size", 100),
                    expansion.getInt("baltop.cache_delay", 60),
                    provider
            );
            this.baltop.start(plugin);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canRegister() {
        return provider != null;
    }

    @Override
    public @Nullable String onRequest(
            @Nullable OfflinePlayer player, @NotNull String param
    ) {
        if (provider != null) {
            if (!baltop.isEnabled() && param.startsWith("top_")) {
                return (param.startsWith("top_balance")) ? "0" : "";
            }

            if (param.contains("top_balance")) {
                if (param.startsWith("top_balance_fixed")) {
                    String currencyId = param.replace("top_balance_fixed_", "");
                    if (currencyId.isEmpty()) {
                        currencyId = provider.getPrimaryCurrencyId();
                    }
                    BigDecimal topBalanceFixed = baltop.getTopBalance(currencyId);
                    if (topBalanceFixed == null) {
                        return "0";
                    } else {
                        return String.valueOf(topBalanceFixed.longValue());
                    }
                }
                if (param.startsWith("top_balance_formatted")) {
                    Locale locale = Locale.ENGLISH;
                    if (player != null && player.isOnline()) {
                        locale = Locale.forLanguageTag(player.getPlayer().getLocale());
                    }
                    int precision;
                    Currency currency;
                    String currencyId = param.replace("top_balance_formatted_", "");
                    if (currencyId.isEmpty()) {
                        currencyId = provider.getPrimaryCurrencyId();
                        currency = provider.getPrimaryCurrency();
                        precision = 2;
                    } else {
                        Optional<Currency> currencyOpt = provider.findCurrency(currencyId);
                        if (currencyOpt.isPresent()) {
                            currency = currencyOpt.get();
                            precision = 2;
                        } else {
                            currency = provider.getPrimaryCurrency();
                            currencyId = provider.getPrimaryCurrencyId();
                            if (currencyId.endsWith("dp")) {
                                try {
                                    precision = Integer.parseInt(currencyId.replace("dp", ""));
                                } catch (NumberFormatException e) {
                                    precision = 2;
                                }
                            } else {
                                precision = 2;
                            }
                        }
                    }
                    BigDecimal topBalanceFormatted = baltop.getTopBalance(currencyId);
                    if (topBalanceFormatted == null) {
                        return "0";
                    } else {
                        return fixMoney(topBalanceFormatted, currency, locale, precision);
                    }
                }
                if (param.startsWith("top_balance_commas")) {
                    String currencyId = param.replace("top_balance_commas_", "");
                    if (currencyId.isEmpty()) {
                        currencyId = provider.getPrimaryCurrencyId();
                    }
                    BigDecimal topBalanceCommas = baltop.getTopBalance(currencyId);
                    if (topBalanceCommas == null) {
                        return "0";
                    } else {
                        return format.format(topBalanceCommas);
                    }
                }

                String currencyId = param.replace("top_balance_", "");
                if (currencyId.isEmpty()) {
                    currencyId = provider.getPrimaryCurrencyId();
                }
                BigDecimal topBalance = baltop.getTopBalance(currencyId);
                if (topBalance == null) {
                    return "0";
                } else {
                    return String.valueOf(topBalance.doubleValue());
                }
            }

            if (param.startsWith("top_player_")) {
                String contestant = param.replace("top_player_", "0");
                String currencyId;
                int position;
                if (contestant.isEmpty()) {
                    currencyId = provider.getPrimaryCurrencyId();
                    position = 1;
                } else {
                    if (contestant.indexOf('_') == -1) {
                        currencyId = contestant;
                        position = 1;
                    } else {
                        String[] split = contestant.split("_");
                        if (split.length == 1) {
                            currencyId = contestant;
                            position = 1;
                        } else if (split.length != 0) {
                            currencyId = split[0];
                            try {
                                position = Integer.parseInt(split[1]);
                            } catch (NumberFormatException e) {
                                position = 1;
                            }
                        } else {
                            currencyId = provider.getPrimaryCurrencyId();
                            position = 1;
                        }
                    }
                }
                return baltop.getTopPlayer(currencyId, position);
            }

            if (player == null) {
                return "";
            }

            if (param.startsWith("top_rank_")) {
                return baltop.getPositionAsString(param.replace("top_rank_", ""), player.getName());
            } else if (param.equalsIgnoreCase("top_rank")) {
                return baltop.getPositionAsString(
                        provider.getPrimaryCurrencyId(),
                        player.getName()
                );
            }

            if (param.contains("balance")) {
                String currencyId;
                if (param.startsWith("balance_")) {
                    currencyId = param.replace("balance_", "");
                } else {
                    currencyId = null;
                }
                Currency currency;
                int precision;
                boolean precisionSetByPlaceholder = false;
                if (currencyId != null) {
                    Optional<Currency> currencyOpt = EconomySubscriber.<Optional<Currency>>asFuture(
                            s -> provider.findCurrency(currencyId)).join();
                    if (currencyOpt.isPresent()) {
                        currency = currencyOpt.get();
                        precision = 2;
                    } else {
                        currency = provider.getPrimaryCurrency();
                        if (currencyId.endsWith("dp")) {
                            try {
                                precision = Integer.parseInt(currencyId.replace("dp", ""));
                                precisionSetByPlaceholder = true;
                            } catch (NumberFormatException e) {
                                precision = 2;
                            }
                        } else {
                            precision = 2;
                        }
                    }
                } else {
                    currency = provider.getPrimaryCurrency();
                    precision = 2;
                }
                Locale locale = player.isOnline() ? Locale.forLanguageTag(player
                        .getPlayer()
                        .getLocale()) : Locale.ENGLISH;
                BigDecimal balance = EconomySubscriber
                        .<Boolean>asFuture(s -> provider.hasPlayerAccount(player.getUniqueId(), s))
                        .thenCompose(val -> {
                            if (val) {
                                return EconomySubscriber.<PlayerAccount>asFuture(s -> provider.retrievePlayerAccount(
                                        player.getUniqueId(),
                                        s
                                ));
                            } else {
                                return EconomySubscriber.<PlayerAccount>asFuture(s -> provider.createPlayerAccount(
                                        player.getUniqueId(),
                                        s
                                ));
                            }
                        })
                        .thenCompose(account -> EconomySubscriber.<BigDecimal>asFuture(s -> account.retrieveBalance(
                                currency,
                                s
                        )))
                        .join();

                if (precisionSetByPlaceholder) {
                    return currency.format(balance, locale, precision);
                }

                if (param.equalsIgnoreCase("balance") || param.equalsIgnoreCase("balance_commas")) {
                    return format.format(balance);
                }
                if (param.equalsIgnoreCase("balance_fixed")) {
                    return String.valueOf(balance.longValue());
                }
                if (param.equalsIgnoreCase("balance_formatted")) {
                    return fixMoney(balance, currency, locale, precision);
                }
            }
        }
        return null;
    }

    private String fixMoney(BigDecimal decimal, Currency currency, Locale locale, int precision) {
        double val = decimal.doubleValue();
        if (val < 1000) {
            return currency.format(decimal, locale, precision);
        }
        if (val < 1000000) {
            String format = currency.format(BigDecimal.valueOf(val / 1000), locale, precision);
            if (format.endsWith(k) || format.endsWith("k")) {
                return format;
            } else {
                return format + k;
            }
        }
        if (val < 1000000000) {
            String format = currency.format(BigDecimal.valueOf(val / 1000000), locale, precision);
            if (format.endsWith(m) || format.endsWith("M")) {
                return format;
            } else {
                return format + m;
            }
        }
        if (val < 1000000000000L) {
            String format = currency.format(
                    BigDecimal.valueOf(val / 1000000000L),
                    locale,
                    precision
            );
            if (format.endsWith(b) || format.endsWith("B")) {
                return format;
            } else {
                return format + b;
            }
        }
        if (val < 1000000000000000L) {
            String format = currency.format(
                    BigDecimal.valueOf(val / 1000000000000L),
                    locale,
                    precision
            );
            if (format.endsWith(t) || format.endsWith("T")) {
                return format;
            } else {
                return format + t;
            }
        }
        if (val < 1000000000000000000L) {
            String format = currency.format(
                    BigDecimal.valueOf(val / 1000000000000000L),
                    locale,
                    precision
            );
            if (format.endsWith(q) || format.endsWith("Q")) {
                return format;
            } else {
                return format + q;
            }
        }

        return String.valueOf((long) val);
    }

}
