package org.payid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.payid.AbstractPayId.upperCasePercentEncoded;

import org.junit.Test;
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
public class PayIdValidValuesTest {

  private String sourcePayId;
  private String expectedAccountPart;
  private String expectedHost;
  private String expectedPayIdToString;

  public PayIdValidValuesTest(
    String sourcePayId,
    String expectedAccountPart,
    String expectedHost,
    String expectedPayIdToString
  ) {
    this.sourcePayId = Objects.requireNonNull(sourcePayId);
    this.expectedAccountPart = Objects.requireNonNull(expectedAccountPart);
    this.expectedHost = Objects.requireNonNull(expectedHost);
    this.expectedPayIdToString = Objects.requireNonNull(expectedPayIdToString);
  }

  /**
   * The data for this test...
   */
  @Parameters
  public static Collection<Object[]> data() {

    return Arrays.asList(new Object[][]{
      //0
      {
        "payid:alice$example.com", // input
        "alice", // accountPart
        "example.com", // host
        "payid:alice$example.com", // expectedToString
      },
      //1 (unreserved chars)
      {
        "payid:abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~$example.net", // input
        "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz0123456789-._~", // accountPart
        "example.net", // host
        "payid:abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz0123456789-._~$example.net", // expectedToString
      },
      //2 (sub-delims)
      {
        "payid:!&'()*+,;=$example.net", // input
        "!&'()*+,;=", // accountPart
        "example.net", // host
        "payid:!&'()*+,;=$example.net", // expectedToString
      },
      //3 (sub-delims first, then unreserved)
      {
        "payid:!alice$example.net", // input
        "!alice", // accountPart
        "example.net", // host
        "payid:!alice$example.net", // expectedToString
      },
      //4 (unreserved first, then sub-delims)
      {
        "payid:aliCE$!example.net", // input
        "alice", // accountPart
        "!example.net", // host
        "payid:alice$!example.net", // expectedToString
      },
      //5 (percent-encoded $)
      {
        "payid:alice%24wallet.example$bank.example.net", // input
        "alice%24wallet.example", // accountPart
        "bank.example.net", // host
        "payid:alice%24wallet.example$bank.example.net", // expectedToString
      },
      //6 (encoded IDN)
      {
        "payid:alice$nic.xn--rovu88b", // input
        "alice", // accountPart
        "nic.xn--rovu88b", // host
        "payid:alice$nic.xn--rovu88b", // expectedToString
      },
      //7 (host with port)
      {
        "payid:alice$example.com:8080", // input
        "alice", // accountPart
        "example.com:8080", // host
        "payid:alice$example.com:8080", // expectedToString
      },
      //8 (Capitalized PAYID)
      {
        "PAYID:alice$example.com:8080", // input
        "alice", // accountPart
        "example.com:8080", // host
        "payid:alice$example.com:8080", // expectedToString
      },
      //9 (accept ":" in host)
      {
        "payid:alice$:example.com", // input
        "alice", // accountPart
        ":example.com", // host
        "payid:alice$:example.com", // expectedToString
      }
    });
  }

  @Test
  public void testValidValues() {
    final PayId payId = PayId.of(sourcePayId);

    assertThat(payId).isNotNull();
    assertThat(payId.account()).isEqualTo(expectedAccountPart);
    assertThat(payId.host()).isEqualTo(expectedHost);
    assertThat(payId.toString()).isEqualTo(expectedPayIdToString);
  }

  @Test
  public void testEquality() {
    final String firstPayId = "payid:alice$sub1.example.net";
    final String secondPayId = "payid:alice$sub2.example.net";

    assertThat(firstPayId).isNotEqualTo(secondPayId);
    assertThat(secondPayId).isNotEqualTo(firstPayId);
    assertThat(firstPayId).isEqualTo(PayId.of("payid:alice$sub1.example.net").toString());
    assertThat(firstPayId).isNotEqualTo(PayId.of("payid:alice$sub2.example.net").toString());
  }

  @Test
  public void testupperCasePercentEncoded() {
    assertThat(upperCasePercentEncoded("foo")).isEqualTo("foo");
    assertThat(upperCasePercentEncoded("Foo$%af%2eBAR")).isEqualTo("Foo$%AF%2EBAR");
    assertThat(upperCasePercentEncoded("foo$%af%2ebar")).isEqualTo("foo$%AF%2Ebar");
  }
}

// TODO: Ensure no $ in sub-delims?
// TODO: Ensure that payid always starts with (unreserved / sub-delims)
// TODO: pct-encoded
// TODO: Invalid (gen-delims like hash)
// TODO: normalization (caps, etc).
// TODO: I18N?
