/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/lokka30/Treasury>.
 */

package me.lokka30.treasury.api.economy.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.lokka30.treasury.api.common.misc.TriState;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.currency.Currency;
import me.lokka30.treasury.api.economy.response.EconomyException;
import me.lokka30.treasury.api.economy.response.EconomySubscriber;
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction;
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionImportance;
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionInitiator;
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An Account is something that holds a balance and is associated with
 * something bound by a UUID. For example, a PlayerAccount is bound to
 * a Player on a server by their UUID.
 *
 * @author lokka30, Geolykt, creatorfromhell
 * @see EconomyProvider
 * @see PlayerAccount
 * @see NonPlayerAccount
 * @since v1.0.0
 */
public interface Account {

    /**
     * Gets the string-based unique identifier for this account.
     *
     * @return The String unique identifier for this account.
     * @since v1.0.0
     */
    @NotNull String getIdentifier();

    /**
     * Returns the name of this {@link Account}, if specified. Empty optional otherwise.
     *
     * <p>A economy provider may choose not to provide a name.
     *
     * @return an optional fulfilled with a name or an empty optional
     * @since v1.0.0
     */
    Optional<String> getName();

    /**
     * Sets a new name for this {@link Account}, which may be null.
     *
     * @param name         the new name for this account.
     * @param subscription the {@link EconomySubscriber} accepting whether name change was successful
     * @since v1.0.0
     */
    void setName(@Nullable String name, @NotNull EconomySubscriber<Boolean> subscription);

    /**
     * {@link #setName(String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> setName(@Nullable String name) {
        return EconomySubscriber.asFuture(s -> setName(name, s));
    }

    /**
     * Request the balance of the {@code Account}.
     *
     * @param currency     the {@link Currency} of the balance being requested
     * @param subscription the {@link EconomySubscriber} accepting the amount
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @since v1.0.0
     */
    void retrieveBalance(
            @NotNull Currency currency, @NotNull EconomySubscriber<BigDecimal> subscription
    );

    /**
     * {@link #retrieveBalance(Currency, EconomySubscriber)}}
     */
    @NotNull
    default CompletableFuture<BigDecimal> retrieveBalance(@NotNull Currency currency) {
        return EconomySubscriber.asFuture(s -> retrieveBalance(currency, s));
    }

    /**
     * Set the balance of the {@code Account}.
     *
     * @param amount       the amount the new balance will be
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being set
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#retrieveBalance(Currency, EconomySubscriber)
     * @since v1.0.0
     */
    void setBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomySubscriber<BigDecimal> subscription
    );

    /**
     * {@link #setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> setBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency
    ) {
        return EconomySubscriber.asFuture(s -> setBalance(amount, initiator, currency, s));
    }

    /**
     * Withdraw an amount from the {@code Account} balance.
     *
     * @param amount       the amount the balance will be reduced by
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being modified
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#doTransaction(EconomyTransaction, EconomySubscriber)
     * @since v1.0.0
     */
    default void withdrawBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        withdrawBalance(
                amount,
                initiator,
                currency,
                EconomyTransactionImportance.NORMAL,
                null,
                subscription
        );
    }

    /**
     * {@link #withdrawBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> withdrawBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency
    ) {
        return EconomySubscriber.asFuture(s -> withdrawBalance(amount, initiator, currency, s));
    }

    /**
     * Withdraw an amount from the {@code Account} balance.
     *
     * @param amount       the amount the balance will be reduced by
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being modified
     * @param importance   how important is the transaction
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#doTransaction(EconomyTransaction, EconomySubscriber)
     * @since v1.0.0
     */
    default void withdrawBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        withdrawBalance(amount, initiator, currency, importance, null, subscription);
    }

    /**
     * {@link #withdrawBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomyTransactionImportance, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> withdrawBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance
    ) {
        return EconomySubscriber.asFuture(s -> withdrawBalance(
                amount,
                initiator,
                currency,
                importance,
                s
        ));
    }

    /**
     * Withdraw an amount from the {@code Account} balance.
     *
     * @param amount       the amount the balance will be reduced by
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being modified
     * @param importance   how important is the transaction
     * @param reason       the reason of why the balance is modified
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#doTransaction(EconomyTransaction, EconomySubscriber)
     * @since v1.0.0
     */
    default void withdrawBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance,
            @Nullable String reason,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        doTransaction(EconomyTransaction
                .newBuilder()
                .withCurrency(currency)
                .withInitiator(initiator)
                .withReason(reason)
                .withTransactionAmount(amount)
                .withImportance(importance)
                .withTransactionType(EconomyTransactionType.WITHDRAWAL)
                .build(), subscription);
    }

    /**
     * {@link #withdrawBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomyTransactionImportance, String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> withdrawBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance,
            @Nullable String reason
    ) {
        return EconomySubscriber.asFuture(s -> withdrawBalance(
                amount,
                initiator,
                currency,
                importance,
                reason,
                s
        ));
    }

    /**
     * Deposit an amount into the {@code Account} balance.
     *
     * @param amount       the amount the balance will be increased by
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being modified
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#doTransaction(EconomyTransaction, EconomySubscriber)
     * @since v1.0.0
     */
    default void depositBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        depositBalance(
                amount,
                initiator,
                currency,
                EconomyTransactionImportance.NORMAL,
                null,
                subscription
        );
    }

    /**
     * {@link #depositBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> depositBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency
    ) {
        return EconomySubscriber.asFuture(s -> depositBalance(amount, initiator, currency, s));
    }

    /**
     * Deposit an amount into the {@code Account} balance.
     *
     * @param amount       the amount the balance will be increased by
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being modified
     * @param importance   how important is the transaction
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#doTransaction(EconomyTransaction, EconomySubscriber)
     * @since v1.0.0
     */
    default void depositBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        depositBalance(amount, initiator, currency, importance, null, subscription);
    }

    /**
     * {@link #depositBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomyTransactionImportance, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> depositBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance
    ) {
        return EconomySubscriber.asFuture(s -> depositBalance(
                amount,
                initiator,
                currency,
                importance,
                s
        ));
    }

    /**
     * Deposit an amount into the {@code Account} balance.
     *
     * @param amount       the amount the balance will be increased by
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being modified
     * @param importance   how important is the transaction
     * @param reason       the reason of why the balance is modified
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#doTransaction(EconomyTransaction, EconomySubscriber)
     * @since v1.0.0
     */
    default void depositBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance,
            @Nullable String reason,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        doTransaction(EconomyTransaction
                .newBuilder()
                .withCurrency(currency)
                .withInitiator(initiator)
                .withTransactionAmount(amount)
                .withReason(reason)
                .withImportance(importance)
                .withTransactionType(EconomyTransactionType.DEPOSIT)
                .build(), subscription);
    }

    /**
     * {@link #depositBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomyTransactionImportance, String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> depositBalance(
            @NotNull BigDecimal amount,
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomyTransactionImportance importance,
            @Nullable String reason
    ) {
        return EconomySubscriber.asFuture(s -> depositBalance(
                amount,
                initiator,
                currency,
                importance,
                reason,
                s
        ));
    }

    /**
     * Does a {@link EconomyTransaction} on this account.
     *
     * @param economyTransaction the transaction that should be done
     * @param subscription       the {@link EconomySubscriber} accepting the new balance
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @since v1.0.0
     */
    void doTransaction(
            @NotNull EconomyTransaction economyTransaction,
            @NotNull EconomySubscriber<BigDecimal> subscription
    );

    /**
     * {@link #doTransaction(EconomyTransaction, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> doTransaction(@NotNull EconomyTransaction economyTransaction) {
        return EconomySubscriber.asFuture(s -> doTransaction(economyTransaction, s));
    }

    /**
     * Reset the {@code Account} balance to its starting amount.
     *
     * <p>Certain implementations, such as the {@link PlayerAccount}, may default to non-zero starting balances.
     *
     * @param initiator    the one who initiated the transaction
     * @param currency     the {@link Currency} of the balance being reset
     * @param subscription the {@link EconomySubscriber} accepting the new balance
     * @see PlayerAccount#resetBalance(EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @see Account#setBalance(BigDecimal, EconomyTransactionInitiator, Currency, EconomySubscriber)
     * @since v1.0.0
     */
    default void resetBalance(
            @NotNull EconomyTransactionInitiator<?> initiator,
            @NotNull Currency currency,
            @NotNull EconomySubscriber<BigDecimal> subscription
    ) {
        setBalance(BigDecimal.ZERO, initiator, currency, new EconomySubscriber<BigDecimal>() {
            @Override
            public void succeed(@NotNull BigDecimal value) {
                subscription.succeed(BigDecimal.ZERO);
            }

            @Override
            public void fail(@NotNull EconomyException exception) {
                subscription.fail(exception);
            }
        });
    }

    /**
     * {@link #resetBalance(EconomyTransactionInitiator, Currency, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> resetBalance(
            @NotNull EconomyTransactionInitiator<?> initiator, @NotNull Currency currency
    ) {
        return EconomySubscriber.asFuture(s -> resetBalance(initiator, currency, s));
    }

    /**
     * Check if the {@code Account} can afford a withdrawal of a certain amount.
     *
     * @param amount       the amount the balance must meet or exceed
     * @param currency     the {@link Currency} of the balance being queried
     * @param subscription the {@link EconomySubscriber} accepting whether the balance is high enough
     * @see Account#retrieveBalance(Currency, EconomySubscriber)
     * @since v1.0.0
     */
    default void canAfford(
            @NotNull BigDecimal amount,
            @NotNull Currency currency,
            @NotNull EconomySubscriber<Boolean> subscription
    ) {
        retrieveBalance(currency, new EconomySubscriber<BigDecimal>() {
            @Override
            public void succeed(@NotNull BigDecimal value) {
                subscription.succeed(value.compareTo(amount) >= 0);
            }

            @Override
            public void fail(@NotNull EconomyException exception) {
                subscription.fail(exception);
            }
        });
    }

    /**
     * {@link #canAfford(BigDecimal, Currency, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> canAfford(@NotNull BigDecimal amount,
                                                 @NotNull Currency currency) {
        return EconomySubscriber.asFuture(s -> canAfford(amount, currency, s));
    }

    /**
     * Delete data stored for the {@code Account}.
     *
     * <p>Providers should consider storing backups of deleted accounts.
     *
     * @param subscription the {@link EconomySubscriber} accepting whether deletion occurred successfully
     * @since v1.0.0
     */
    void deleteAccount(@NotNull EconomySubscriber<Boolean> subscription);

    /**
     * {@link #deleteAccount(EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> deleteAccount() {
        return EconomySubscriber.asFuture(this::deleteAccount);
    }

    /**
     * Returns the {@link Currency#getIdentifier()  Currencies} this {@code Account} holds balance for.
     *
     * @param subscription the {@link EconomySubscriber} accepting the currencies
     * @since v1.0.0
     */
    void retrieveHeldCurrencies(@NotNull EconomySubscriber<Collection<String>> subscription);

    /**
     * {@link #retrieveHeldCurrencies(EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<String>> retrieveHeldCurrencies() {
        return EconomySubscriber.asFuture(this::retrieveHeldCurrencies);
    }

    /**
     * Request the {@link EconomyTransaction} history, limited by the {@code transactionCount} and the {@link Temporal}
     * {@code from} and {@link Temporal} {@code to}, of this {@code Account}.
     *
     * <p>If the specified {@code transactionCount} is higher than the known transactions, then this will return all the
     * transactions.
     * <p>If this account does not have transactions, dating back to the specified {@code from}, it will start returning
     * transactions from the oldest one.
     *
     * @param transactionCount the count of the transactions wanted
     * @param from             the timestamp to get the transactions from
     * @param to               the timestamp to get the transactions to
     * @param subscription     the {@link EconomySubscriber} accepting the transaction history
     * @since v1.0.0
     */
    void retrieveTransactionHistory(
            int transactionCount,
            @NotNull Temporal from,
            @NotNull Temporal to,
            @NotNull EconomySubscriber<Collection<EconomyTransaction>> subscription
    );

    /**
     * {@link #retrieveTransactionHistory(int, Temporal, Temporal, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<EconomyTransaction>> retrieveTransactionHistory(
            int transactionCount, @NotNull Temporal from, @NotNull Temporal to
    ) {
        return EconomySubscriber.asFuture(s -> retrieveTransactionHistory(
                transactionCount,
                from,
                to,
                s
        ));
    }

    /**
     * Request the {@link EconomyTransaction} history, limited by the {@code transactionCount}, of this {@code Account}.
     *
     * <p>If the specified {@code transactionCount} is higher than the known transactions, then this will return all the
     * transactions.
     *
     * @param transactionCount the count of the transactions wanted
     * @param subscription     the {@link EconomySubscriber} accepting the transaction history
     * @since v1.0.0
     */
    default void retrieveTransactionHistory(
            int transactionCount,
            @NotNull EconomySubscriber<Collection<EconomyTransaction>> subscription
    ) {
        retrieveTransactionHistory(transactionCount, Instant.EPOCH, Instant.now(), subscription);
    }

    /**
     * {@link #retrieveTransactionHistory(int, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<EconomyTransaction>> retrieveTransactionHistory(int transactionCount) {
        return EconomySubscriber.asFuture(s -> retrieveTransactionHistory(transactionCount, s));
    }

    /**
     * Request a listing of all member players of the account.
     *
     * @param subscription the {@link EconomySubscriber} accepting the members
     * @since v1.0.0
     */
    void retrieveMemberIds(@NotNull EconomySubscriber<Collection<UUID>> subscription);

    /**
     * {@link #retrieveMemberIds(EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Collection<UUID>> retrieveMemberIds() {
        return EconomySubscriber.asFuture(this::retrieveMemberIds);
    }

    /**
     * Check if the specified user is a member of the account.
     *
     * <p>A member is any player with at least one allowed permission.
     *
     * @param player       the {@link UUID} of the potential member
     * @param subscription the {@link EconomySubscriber} accepting whether the user is a member
     * @since v1.0.0
     */
    void isMember(@NotNull UUID player, @NotNull EconomySubscriber<Boolean> subscription);

    /**
     * {@link #isMember(UUID, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Boolean> isMember(@NotNull UUID player) {
        return EconomySubscriber.asFuture(s -> isMember(player, s));
    }

    /**
     * Modifies the state of the specified {@link AccountPermission} {@code permissions} for the
     * specified {@link UUID} {@code player}.
     * The state of the permission is specified via the {@code permissionValue} boolean, where
     * {@code TRUE} is 'has permission', and {@code FALSE} is 'does not have permission'.
     * Just a reminder: a member is any player with at least one allowed permission.
     *
     * @param player          the player id you want to modify the permissions of
     * @param permissionValue the permission value you want to set
     * @param subscription    the {@link EconomySubscriber} accepting if the permissions of the
     *                        member were adjusted.
     * @param permissions     the permissions to modify
     * @since v1.0.0
     */
    void setPermission(
            @NotNull UUID player,
            @NotNull TriState permissionValue,
            @NotNull EconomySubscriber<TriState> subscription,
            @NotNull AccountPermission @NotNull ... permissions
    );

    /**
     * {@link #setPermission(UUID, TriState, EconomySubscriber, AccountPermission...)}
     */
    @NotNull
    default CompletableFuture<TriState> setPermission(
            @NotNull UUID player,
            @NotNull TriState permissionValue,
            @NotNull AccountPermission @NotNull ... permissions
    ) {
        return EconomySubscriber.asFuture(s -> setPermission(
                player,
                permissionValue,
                s,
                permissions
        ));
    }

    /**
     * Request the {@link AccountPermission AccountPermissions} for the specified {@link UUID}
     * {@code player}.
     *
     * @param player       the player {@link UUID} to get the permissions for
     * @param subscription the {@link EconomySubscriber} accepting an immutable map of permissions and their values.
     * @since v1.0.0
     */
    void retrievePermissions(
            @NotNull UUID player,
            @NotNull EconomySubscriber<Map<AccountPermission, TriState>> subscription
    );

    /**
     * {@link #retrievePermissions(UUID, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<Map<AccountPermission, TriState>> retrievePermissions(@NotNull UUID player) {
        return EconomySubscriber.asFuture(s -> retrievePermissions(player, s));
    }

    /**
     * Checks whether given player has the given permission on this account.
     *
     * <p>Just a reminder: a member is any player with at least one allowed permission.
     *
     * @param player       the {@link UUID} of the player to check if they have the permission
     * @param subscription the {@link EconomySubscriber} accepting whether the player has all the specified permissions
     * @param permissions  the permissions to check
     * @see AccountPermission
     * @since v1.0.0
     */
    void hasPermission(
            @NotNull UUID player,
            @NotNull EconomySubscriber<TriState> subscription,
            @NotNull AccountPermission @NotNull ... permissions
    );

    /**
     * {@link #hasPermission(UUID, EconomySubscriber, AccountPermission...)}
     */
    @NotNull
    default CompletableFuture<TriState> hasPermission(
            @NotNull UUID player, @NotNull AccountPermission @NotNull ... permissions
    ) {
        return EconomySubscriber.asFuture(s -> hasPermission(player, s, permissions));
    }

}
