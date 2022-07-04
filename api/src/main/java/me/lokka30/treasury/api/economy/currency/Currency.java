/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/lokka30/Treasury>.
 */

package me.lokka30.treasury.api.economy.currency;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.lokka30.treasury.api.economy.response.EconomySubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One of Treasury's core features is multi-currency support.
 * This allows economy providers and plugins to use different
 * currencies for different transactions, e.g. a daily reward
 * plugin can award players 'Tokens', but a job plugin
 * can award players 'Dollars'. This facilitates a high
 * potential for customization, to create a more interesting
 * economy.
 *
 * @author creatorfromhell, lokka30
 * @since v1.0.0
 */
public interface Currency {

    /**
     * Gets the unique non-user friendly identifier for the currency.
     *
     * @return A unique non-user friendly identifier for the currency.
     * @since v1.0.0
     */
    @NotNull String getIdentifier();

    /**
     * Gets the currency's symbol. This could be something as simple as a dollar sign '$'.
     *
     * @return The currency's symbol.
     * @since v1.0.0
     */
    @NotNull String getSymbol();

    /**
     * Get's the currency's decimal character to be used for formatting purposes.
     *
     * @return The currency's decimal character.
     * @since v1.0.0
     */
    char getDecimal();

    /**
     * Gets the singular form of the currency's user-friendly display name.
     * The singular form is used when the amount is exactly {@code 1.00}.
     *
     * @return The currency's user-friendly display name.
     * @since v1.0.0
     */
    @NotNull String getDisplayNameSingular();

    /**
     * Gets the plural form of the currency's user-friendly display name.
     * The plural form is used when the amount is not exactly {@code 1.00}.
     * If a plural form of the {@link Currency} is not available, the value from
     * {@link Currency#getDisplayNameSingular()} should be returned.
     *
     * @return The plural form of the currency's user-friendly display name.
     * @since v1.0.0
     */
    @NotNull String getDisplayNamePlural();

    /**
     * Gets the currency's default number of decimal digits when formatting this currency.
     *
     * @return The currency's default number of decimal digits when formatting this currency.
     * @since v1.0.0
     */
    int getPrecision();

    /**
     * Checks if this currency is the primary currency to use.
     *
     * @return True if this currency is the primary currency. This method should use a global
     *         context if multi-world support is not present, otherwise it should use the default world
     *         for this check.
     * @since v1.0.0
     */
    boolean isPrimary();

    /**
     * Used to convert this {@link Currency} to another based on a specified amount of the other
     * currency.
     *
     * @param currency     The currency we are converting to.
     * @param amount       The amount to be converted to the specified {@link Currency}
     * @param subscription The {@link EconomySubscriber} accepting the resulting {@link BigDecimal} that
     *                     represents the converted amount of the specified {@link Currency}.
     * @since v1.0.0
     */
    void to(
            @NotNull Currency currency,
            @NotNull BigDecimal amount,
            @NotNull EconomySubscriber<BigDecimal> subscription
    );

    /**
     * {@link #to(Currency, BigDecimal, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> to(
            @NotNull Currency currency,
            @NotNull BigDecimal amount
    ) {
        return EconomySubscriber.asFuture(s -> to(currency, amount, s));
    }

    /**
     * Used to get the BigDecimal representation of an amount represented by a formatted string.
     * If the {@code formatted} value can't be parsed, then
     * {@link me.lokka30.treasury.api.economy.response.EconomyFailureReason#NUMBER_PARSING_ERROR}
     * should be used as the {@code FailureReason} for the failure reason in the accompanied
     * {@link me.lokka30.treasury.api.economy.response.EconomyException}.
     *
     * @param formatted    The formatted string to be converted to BigDecimal form.
     * @param subscription The {@link EconomySubscriber} accepting the resulting {@link BigDecimal} that
     *                     represents the deformatted amount of the formatted String.
     * @see me.lokka30.treasury.api.economy.response.EconomyFailureReason#NUMBER_PARSING_ERROR
     * @since v1.0.0
     */
    void parse(@NotNull String formatted, @NotNull EconomySubscriber<BigDecimal> subscription);

    /**
     * {@link #parse(String, EconomySubscriber)}
     */
    @NotNull
    default CompletableFuture<BigDecimal> parse(@NotNull String formatted) {
        return EconomySubscriber.asFuture(s -> parse(formatted, s));
    }

    /**
     * Gets the starting balance of a specific player account for this currency.
     * If a per-player starting balance system is not used, simply ignore the parameter.
     *
     * @param playerID The UUID of the player we are getting the starting balance for. If the
     *                 'general' starting balance of a currency is desired (not for a specific
     *                 player), then specify {@code null} for this parameter.
     * @return The starting balance of the player for this currency.
     * @since v1.0.0
     */
    @NotNull BigDecimal getStartingBalance(@Nullable UUID playerID);

    /**
     * Used to translate an amount to a user readable format with the default precision.
     * If a per-locale format is not used, simply ignore the parameter.
     *
     * @param amount The amount to format.
     * @param locale The locale to use for formatting the balance. This value may be
     *               {@code null} if the provider should provide their 'default' Locale.
     * @return The formatted text.
     * @since v1.0.0
     */
    @NotNull String format(@NotNull BigDecimal amount, @Nullable Locale locale);

    /**
     * Used to translate an amount to a user readable format with the specified amount of decimal places.
     * If a per-locale format is not used, simply ignore the parameter.
     *
     * @param amount    The amount to format.
     * @param locale    The locale to use for formatting the balance. This value may be
     *                  {@code null} if the provider should provide their 'default' Locale.
     * @param precision The amount of decimal digits to use when formatting.
     * @return The formatted text.
     * @since v1.0.0
     */
    @NotNull String format(@NotNull BigDecimal amount, @Nullable Locale locale, int precision);

}
