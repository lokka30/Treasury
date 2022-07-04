/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/lokka30/Treasury>.
 */

package me.lokka30.treasury.api.economy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.lokka30.treasury.api.common.misc.FutureHelper;
import me.lokka30.treasury.api.common.misc.TriState;
import me.lokka30.treasury.api.economy.account.Account;
import me.lokka30.treasury.api.economy.account.AccountPermission;
import me.lokka30.treasury.api.economy.account.NonPlayerAccount;
import me.lokka30.treasury.api.economy.account.PlayerAccount;
import me.lokka30.treasury.api.economy.currency.Currency;
import me.lokka30.treasury.api.economy.misc.EconomyAPIVersion;
import me.lokka30.treasury.api.economy.misc.OptionalEconomyApiFeature;
import me.lokka30.treasury.api.economy.response.EconomyException;
import me.lokka30.treasury.api.economy.response.EconomyFailureReason;
import me.lokka30.treasury.api.economy.response.EconomySubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementors providing and managing economy data create a class
 * which implements this interface to be registered in
 * the specific platform they're implementing it for.
 *
 * @author lokka30, Jikoo, MrIvanPlays, NoahvdAa, creatorfromhell
 * @since v1.0.0
 */
public interface EconomyProvider {

    /**
     * WARNING: API versions are no longer used as of Treasury v1.1.0. Ignore this method.
     *
     * Get the version of the Treasury API the {@code EconomyProvider} is based on.
     *
     * <p>Warning: The Treasury API version is completely different to any other platform version,
     * such as the 'api-version' value in the Bukkit implementation's plugin.yml file.
     * <p>Warning: Do not use {@link EconomyAPIVersion#getCurrentAPIVersion()}, that method is for
     * internal Treasury use only.
     * <b>You must only use the constants provided.</b>
     *
     * @return the API version
     * @since v1.0.0
     * @deprecated API versions are no longer used as of Treasury v1.1.0.
     */
    @Deprecated
    default @NotNull EconomyAPIVersion getSupportedAPIVersion() {
        return EconomyAPIVersion.getCurrentAPIVersion();
    }

    /**
     * Check which optional Treasury Economy API features the Economy Provider supports.
     *
     * <p>There are a few features which Treasury allows Economy Providers to support
     * at their option. This is because certain features within Treasury may not be
     * suitable or appealing for some Economy Providers to implement. We try to enforce
     * as much of the API to be implemented as possible, however, few features can be
     * justified as being optional to implement.
     *
     * <p>The Economy Provider should return a Set of constants representing which of
     * Treasury's optional Economy API features are supported by the Economy Provider.
     * This set can be empty if none of the optional features are supported by the
     * Economy Provider.
     *
     * @return the set of optional supported features from the economy provider/
     * @see OptionalEconomyApiFeature
     * @since v1.0.0
     */
    @NotNull Set<OptionalEconomyApiFeature> getSupportedOptionalEconomyApiFeatures();

    /**
     * Request whether a user has an associated {@link PlayerAccount}.
     *
     * @param accountId    the {@link UUID} of the account owner
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void hasPlayerAccount(
            @NotNull UUID accountId, @NotNull EconomySubscriber<Boolean> subscription
    );

    /**
     * {@link #hasPlayerAccount(UUID, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> hasPlayerAccount(@NotNull UUID accountId) {
        return EconomySubscriber.asFuture(s -> hasPlayerAccount(accountId, s));
    }

    /**
     * Request an existing {@link PlayerAccount} for a user.
     *
     * @param accountId    the {@link UUID} of the account owner
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void retrievePlayerAccount(
            @NotNull UUID accountId, @NotNull EconomySubscriber<PlayerAccount> subscription
    );

    /**
     * {@link #retrievePlayerAccount(UUID, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<PlayerAccount> retrievePlayerAccount(@NotNull UUID accountId) {
        return EconomySubscriber.asFuture(s -> retrievePlayerAccount(accountId, s));
    }

    /**
     * Request the creation of a {@link PlayerAccount} for a user.
     *
     * @param accountId    the {@link UUID} of the account owner
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void createPlayerAccount(
            @NotNull UUID accountId, @NotNull EconomySubscriber<PlayerAccount> subscription
    );

    /**
     * {@link #createPlayerAccount(UUID, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<PlayerAccount> createPlayerAccount(@NotNull UUID accountId) {
        return EconomySubscriber.asFuture(s -> createPlayerAccount(accountId, s));
    }

    /**
     * Request all {@link UUID UUIDs} with associated {@link PlayerAccount PlayerAccounts}.
     *
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void retrievePlayerAccountIds(@NotNull EconomySubscriber<Collection<UUID>> subscription);

    /**
     * {@link #retrievePlayerAccountIds(EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<UUID>> retrievePlayerAccountIds() {
        return EconomySubscriber.asFuture(this::retrievePlayerAccountIds);
    }

    /**
     * Request whether an identifier has an associated {@link Account}.
     * <p>
     * This method is safe for {@link NonPlayerAccount non-player accounts}.
     * <p>
     * This could return an {@link NonPlayerAccount} or an {@link PlayerAccount}.
     *
     * @param identifier   the identifier of the account
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void hasAccount(@NotNull String identifier, @NotNull EconomySubscriber<Boolean> subscription);

    /**
     * {@link #hasAccount(String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> hasAccount(@NotNull String identifier) {
        return EconomySubscriber.asFuture(s -> hasAccount(identifier, s));
    }

    /**
     * Request an existing {@link Account} for a specific identifier.
     * <p>
     * This method is safe for {@link NonPlayerAccount non-player accounts}.
     * <p>
     * This could return an {@link NonPlayerAccount} or an {@link PlayerAccount}.
     *
     * @param identifier   the identifier of the account
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void retrieveAccount(
            @NotNull String identifier, @NotNull EconomySubscriber<Account> subscription
    );

    /**
     * {@link #retrieveAccount(String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Account> retrieveAccount(@NotNull String identifier) {
        return EconomySubscriber.asFuture(s -> retrieveAccount(identifier, s));
    }

    /**
     * Request the creation of a {@link Account} for a specific identifier.
     * <p>
     * This method is safe for {@link NonPlayerAccount non-player accounts}.
     * <p>
     * This could return an {@link NonPlayerAccount} or an {@link PlayerAccount}.
     *
     * @param identifier   the identifier of the account
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    default void createAccount(
            @NotNull String identifier, @NotNull EconomySubscriber<Account> subscription
    ) {
        createAccount(null, identifier, subscription);
    }

    /**
     * {@link #createAccount(String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Account> createAccount(@NotNull String identifier) {
        return EconomySubscriber.asFuture(s -> createAccount(identifier, s));
    }

    /**
     * Request the creation of a {@link Account} for a specific identifier {@code identifier} with {@link String} {@code
     * name}.
     * <p>
     * This method is safe for {@link NonPlayerAccount non-player accounts}.
     * <p>
     * This could return an {@link NonPlayerAccount} or an {@link PlayerAccount}.
     *
     * @param name         the human-readable name of the account
     * @param identifier   the unique identifier of the account
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void createAccount(
            @Nullable String name,
            @NotNull String identifier,
            @NotNull EconomySubscriber<Account> subscription
    );

    /**
     * {@link #createAccount(String, String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Account> createAccount(
            @Nullable String name, @NotNull String identifier
    ) {
        return EconomySubscriber.asFuture(s -> createAccount(name, identifier, s));
    }

    /**
     * Request all identifiers with associated {@link Account Accounts}.
     *
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void retrieveAccountIds(@NotNull EconomySubscriber<Collection<String>> subscription);

    /**
     * {@link #retrieveAccountIds(EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<String>> retrieveAccountIds() {
        return EconomySubscriber.asFuture(this::retrieveAccountIds);
    }

    /**
     * Request all identifiers with associated {@link NonPlayerAccount NonPlayer Accounts}.
     *
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    void retrieveNonPlayerAccountIds(@NotNull EconomySubscriber<Collection<String>> subscription);

    /**
     * {@link #retrieveAccountIds(EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<String>> retrieveNonPlayerAccountIds() {
        return EconomySubscriber.asFuture(this::retrieveNonPlayerAccountIds);
    }

    /**
     * Request all {@link NonPlayerAccount non player accounts} the given player is a member of.
     *
     * @param playerId the player
     * @return future with data
     */
    @NotNull
    default CompletableFuture<Collection<String>> retrieveAllAccountsPlayerIsMemberOf(@NotNull UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return this.retrieveAccountIds().thenComposeAsync(identifiers -> {
            if (identifiers.isEmpty()) {
                return CompletableFuture.completedFuture(Collections.emptySet());
            }
            Collection<CompletableFuture<Account>> futures = new HashSet<>();
            for (String identifier : identifiers) {
                futures.add(this.retrieveAccount(identifier));
            }
            return FutureHelper.mjf(account -> account.isMember(playerId),
                    Account::getIdentifier,
                    futures
            );
        });
    }

    /**
     * Request all {@link NonPlayerAccount non player accounts} the given player is a member of.
     *
     * @param playerId     the player
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @since v1.0.0
     */
    default void retrieveAllAccountsPlayerIsMemberOf(
            @NotNull UUID playerId, @NotNull EconomySubscriber<Collection<String>> subscription
    ) {
        Objects.requireNonNull(subscription, "subscription");
        this.retrieveAllAccountsPlayerIsMemberOf(playerId).whenCompleteAsync((ret, ex) -> {
            if (ex != null) {
                if (ex instanceof EconomyException) {
                    subscription.fail((EconomyException) ex);
                } else {
                    subscription.fail(new EconomyException(EconomyFailureReason.OTHER_FAILURE, ex));
                }
                return;
            }
            subscription.succeed(ret);
        });
    }

    /**
     * Request all the {@link NonPlayerAccount non-player accounts} where the given player has the given permissions.
     *
     * @param playerId     the player
     * @param permissions  the permissions that the given player has to have on the {@link NonPlayerAccount account}
     * @see #retrieveAllAccountsPlayerIsMemberOf(UUID)
     */
    @NotNull
    default CompletableFuture<Collection<String>> retrieveAllAccountsPlayerHasPermission(
            @NotNull UUID playerId, @NotNull AccountPermission @NotNull ... permissions
    ) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(permissions, "permissions");
        return this.retrieveAccountIds().thenComposeAsync(identifiers -> {
            if (identifiers.isEmpty()) {
                return CompletableFuture.completedFuture(Collections.emptySet());
            }
            Collection<CompletableFuture<Account>> accounts = new HashSet<>();
            for (String identifier : identifiers) {
                accounts.add(this.retrieveAccount(identifier));
            }
            return FutureHelper.mjf(
                    account -> account
                            .hasPermission(playerId, permissions)
                            .thenApply(triState -> triState == TriState.TRUE),
                    Account::getIdentifier,
                    accounts
            );
        });
    }

    /**
     * Request all the {@link NonPlayerAccount non-player accounts} where the given player has the given permissions.
     *
     * @param playerId     the player
     * @param subscription the {@link EconomySubscriber} accepting the resulting value
     * @param permissions  the permissions that the given player has to have on the {@link NonPlayerAccount account}
     * @see #retrieveAllAccountsPlayerIsMemberOf(UUID, EconomySubscriber)
     * @since v1.0.0
     */
    default void retrieveAllAccountsPlayerHasPermission(
            @NotNull UUID playerId,
            @NotNull EconomySubscriber<Collection<String>> subscription,
            @NotNull AccountPermission @NotNull ... permissions
    ) {
        Objects.requireNonNull(subscription, "subscription");
        this
                .retrieveAllAccountsPlayerHasPermission(playerId, permissions)
                .whenCompleteAsync((ret, ex) -> {
                    if (ex != null) {
                        if (ex instanceof EconomyException) {
                            subscription.fail((EconomyException) ex);
                        } else {
                            subscription.fail(new EconomyException(EconomyFailureReason.OTHER_FAILURE,
                                    ex
                            ));
                        }
                        return;
                    }
                    subscription.succeed(ret);
                });
    }

    /**
     * Get the primary or main {@link Currency} of the economy.
     *
     * @return the primary currency
     * @since v1.0.0
     */
    @NotNull Currency getPrimaryCurrency();

    /**
     * Used to find a currency based on a specific identifier.
     *
     * @param identifier The {@link Currency#getIdentifier()} of the {@link Currency} we are searching for.
     * @return The {@link Optional} containing the search result. This will contain the
     *         resulting {@link Currency} if it exists, otherwise it will return {@link Optional#empty()}.
     * @since v1.0.0
     */
    Optional<Currency> findCurrency(@NotNull String identifier);

    /**
     * Used to get a set of every  {@link Currency} object for the server.
     *
     * @return A set of every {@link Currency} object that is available for the server.
     * @since v1.0.0
     */
    Set<Currency> getCurrencies();

    /**
     * Get the String identifier of the primary or main {@link Currency} of the economy.
     *
     * @return the String identifier identifying the primary currency
     * @since v1.0.0
     */
    @NotNull
    default String getPrimaryCurrencyId() {
        return getPrimaryCurrency().getIdentifier();
    }

    /**
     * Used to register a currency with the {@link EconomyProvider} to be utilized by
     * other plugins.
     *
     * @param currency     The currency to register with the {@link EconomyProvider}.
     * @param subscription The {@link EconomySubscriber} representing the result of the
     *                     attempted {@link Currency} registration with an {@link Boolean}.
     *                     This will be {@link Boolean#TRUE} if it was registered, otherwise
     *                     it'll be {@link Boolean#FALSE}.
     * @since v1.0.0
     */
    void registerCurrency(
            @NotNull Currency currency, @NotNull EconomySubscriber<Boolean> subscription
    );

    /**
     * {@link #registerCurrency(Currency, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> registerCurrency(@NotNull Currency currency) {
        return EconomySubscriber.asFuture(s -> registerCurrency(currency, s));
    }

}
