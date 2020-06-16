package org.payid;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract implementation of {@link PayId} for use by Immutables.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePayId.class)
@JsonDeserialize(as = ImmutablePayId.class)
abstract class AbstractPayId implements PayId {

  private static final String ALPHA = "a-zA-Z";
  private static final String DIGIT = "0-9";
  private static final String UNRESERVED = ALPHA + DIGIT + "\\-\\.\\_\\~";
  private static final String SUB_DELIMS = "\\!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=";
  private static final String PERCENT = "\\%";

  // Note: These regex's don't enforce any ordering or format for things like IP address, etc. They merely define
  // allowed characters in a PayID. A PayID must be dereferenced in order to determine if it's a valid host
  // per RFC-3986.

  // ACCOUNT (allowed-chars)
  private static final String ALLOWED_ACCOUNT_CHARS = UNRESERVED + SUB_DELIMS + PERCENT + ":@/";
  private static final String ALLOWED_ACCOUNT_CHARS_REGEX = "^([" + ALLOWED_ACCOUNT_CHARS + "]+)*$";

  // HOST (allowed-chars)
  // IP-literal / IPv4address / reg-name
  private static final String HEX_DIGITS = "[[:xdigit:]]";
  private static final String IPV4_ADDRESS = DIGIT + "."; // Hex is already allowed via IP_LITERAL
  private static final String IPV6_ADDRESS = ":\\." + HEX_DIGITS;
  // reg-name syntax allows percent-encoded octets in order to represent non-ASCII registered names in a uniform way
  // that is independent of the underlying name resolution technology
  private static final String REG_NAME = UNRESERVED + PERCENT + SUB_DELIMS;

  // IPVFUTURE and IP_LITERAL are not accounted for.
  private static final String ALLOWED_HOST_CHARS = IPV4_ADDRESS + IPV6_ADDRESS + REG_NAME;
  private static final String ALLOWED_HOST_CHARS_REGEX = "^([" + ALLOWED_HOST_CHARS + "]+)*$";

  // Regex
  private static final Pattern ACCOUNT_PATTERN = Pattern.compile(ALLOWED_ACCOUNT_CHARS_REGEX);
  private static final Pattern HOST_PATTERN = Pattern.compile(ALLOWED_HOST_CHARS_REGEX);

  // For upper-casing HEX
  private static final Pattern PERCENT_ENCODED_PATTERN = Pattern.compile("(%[0-9a-fA-F][0-9a-fA-F])");

  /**
   * For a String {@code input}, find any lower-cased percent-encoded values (e.g., %3a) and upper-case them (e.g., %3A)
   * in order to conform to the rules of RFC-3986.
   *
   * @param input A {@link String} that might have percent-encoded triplets.
   *
   * @return A {@link String} with any percent-encoded triplets normalized to upper-case form.
   *
   * @see "https://tools.ietf.org/html/rfc3986#section-6.2.2.1"
   */
  static String upperCasePercentEncoded(String input) {
    // Upper-case any percent-encoded triplets (e.g., `%3a` -> `%3A`).
    final Matcher matcher = PERCENT_ENCODED_PATTERN.matcher(input);

    final StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(result, matcher.group().toUpperCase());
    }
    matcher.appendTail(result);

    String returnable = result.toString();
    return returnable;
  }

  /**
   * Validate a PayID per the rules defined in the PayID RFC.
   *
   * @return A normalized and valid {@link AbstractPayId}.
   */
  @Check
  AbstractPayId validate() {

    // Verify Account
    Preconditions.checkArgument(
      ACCOUNT_PATTERN.matcher(this.account()).matches(),
      format("PayID 'account' for `%s` has an invalid value.", this.toString())
    );

    // Verify Host
    Preconditions.checkArgument(HOST_PATTERN.matcher(this.host()).matches(),
      format("PayID 'host' for `%s` has an invalid value.", this.toString()));

    return this;
  }

  @Override
  public String toString() {
    return PAYID_SCHEME + account() + "$" + host();
  }
}
