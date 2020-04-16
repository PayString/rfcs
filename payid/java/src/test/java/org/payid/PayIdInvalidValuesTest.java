package org.payid;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Unit tests for {@link PayId}.
 */
@RunWith(Parameterized.class)
public class PayIdInvalidValuesTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private String sourcepayid;
  private Class<Exception> exceptionClassToExpect;
  private String exceptionMessageToExpect;

  public PayIdInvalidValuesTest(
      final String sourcepayid, final Class<Exception> exceptionToExpect, final String exceptionMessageToExpect
  ) {
    this.sourcepayid = sourcepayid;
    this.exceptionClassToExpect = Objects.requireNonNull(exceptionToExpect);
    this.exceptionMessageToExpect = exceptionMessageToExpect;
  }

  /**
   * The data for this test...
   */
  @Parameters
  public static Collection<Object[]> data() {

    return Arrays.asList(new Object[][] {

        /////////////////
        // Invalid Values
        /////////////////

        //0
        {
            null, // input
            NullPointerException.class, // exception to expect
            "PayID must not be null", // message to expect
        },
        //1
        {
            "$", // input (missing host)
            IllegalArgumentException.class, // exception to expect
            "PayID `$` must start with the 'payid:' scheme", // message to expect
        },
        //2
        {
            "payid:$", // input (missing host)
            IllegalArgumentException.class, // exception to expect
            "PayID `$` must specify a valid account and host", // message to expect
        },
        //3
        {
            "alice$wallet.example", // input missing payid: prefix
            IllegalArgumentException.class, // exception to expect
            "PayID `alice$wallet.example` must start with the 'payid:' scheme", // message to expect
        },
        //4
        {
            "payid:alice$wallet.example$bank.example", // input with two dollar-signs
            IllegalArgumentException.class, // exception to expect
            "PayID `alice$wallet.example$bank.example` may only contain a single dollar-sign. "
                + "All other dollar-signs must be percent-encoded.",
            // message to expect
        },
        //5
        {
            "payid:alice$nic.書籍", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:alice$nic.書籍` has an invalid value.",// message to expect
        },
        //6
        {
            "payid:書籍$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:書籍$example.com` has an invalid value.",// message to expect
        },
        //7 (don't accept gen-delims: ":")
        {
            "payid::$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid::$example.com` has an invalid value.",// message to expect
        },
        //8 (don't accept gen-delims "/")
        {
            "payid:/$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:/$example.com` has an invalid value.",// message to expect
        },
        //9 (don't accept gen-delims "/")
        {
            "payid:alice$/example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:alice$/example.com` has an invalid value.",// message to expect
        },
        //10 (don't accept gen-delims "?")
        {
            "payid:?$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:?$example.com` has an invalid value.",// message to expect
        },
        //11 (don't accept gen-delims "?")
        {
            "payid:a$?example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:a$?example.com` has an invalid value.",// message to expect
        },
        //12 (don't accept gen-delims "#")
        {
            "payid:#$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:#$example.com` has an invalid value.",// message to expect
        },
        //13 (don't accept gen-delims "#")
        {
            "payid:a$#example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:a$#example.com` has an invalid value.",// message to expect
        },
        //14 (don't accept gen-delims "[" in account)
        {
            "payid:[$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:[$example.com` has an invalid value.",// message to expect
        },
        //15 (don't accept gen-delims "[" in host)
        {
            "payid:alice$[example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:alice$[example.com` has an invalid value.",// message to expect
        },
        //16 (don't accept gen-delims "]" in account)
        {
            "payid:]$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:]$example.com` has an invalid value.",// message to expect
        },
        //17 (don't accept gen-delims "]" in host)
        {
            "payid:alice$]example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:alice$]example.com` has an invalid value.",// message to expect
        },
        //18 (don't accept gen-delims "@")
        {
            "payid:@$example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:@$example.com` has an invalid value.",// message to expect
        },
        //19 (don't accept gen-delims "@")
        {
            "payid:a$@example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'host' for `payid:a$@example.com` has an invalid value.",// message to expect
        },
        //20 (don't accept payid that start with a %-encoded value.)
        {
            "payid:%20abc$@example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID `%20abc$@example.com` MUST start with either an 'unreserved' or 'sub-selims' character rules. "
                + "A PayID may not start with a percent-encoded value.", // message to expect
        },
        //21 (don't accept payid that start with a %-encoded value.)
        {
            "payid:%20abc$@example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID `%20abc$@example.com` MUST start with either an 'unreserved' or 'sub-selims' character rules. "
                + "A PayID may not start with a percent-encoded value.",// message to expect
        },
        //22 (don't accept payid with a '^'.)
        {
            "payid:alice^$@example.com", // input
            IllegalArgumentException.class, // exception to expect
            "PayID 'account' for `payid:alice^$@example.com` has an invalid value.",// message to expect
        },
        //23 (empty account)
        {
            "payid:$@example.com", // input
            IllegalStateException.class, // exception to expect
            "Cannot build PayId, some of required attributes are not set [account]",// message to expect
        },
        //24 (empty account with path)
        {
            "payid:$example.com/bar", // input
            IllegalStateException.class, // exception to expect
            "Cannot build PayId, some of required attributes are not set [account]",// message to expect
        },
        //25 (empty account with empty path)
        {
            "payid:$example.com/", // input
            IllegalStateException.class, // exception to expect
            "Cannot build PayId, some of required attributes are not set [account]",// message to expect
        },
        //26 (empty account with double path)
        {
            "payid:$rafiki.money/p/test@example.com", // input
            IllegalStateException.class, // exception to expect
            "Cannot build PayId, some of required attributes are not set [account]",// message to expect
        }
    });
  }

  @Test
  public void testInvalidValues() {
    expectedException.expect(exceptionClassToExpect);
    expectedException.expectMessage(exceptionMessageToExpect);

    PayId.of(sourcepayid);
  }
}
