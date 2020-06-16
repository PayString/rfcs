package org.payid;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import okhttp3.HttpUrl;
import org.immutables.value.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

/**
 * Validates that all public suffixes work properly in a {@link PayId}.
 */
@RunWith(Parameterized.class)
public class PayIdPublicSuffixesTest {

  private ValidPublicTldTestVector validPublicTldTestVector;

  public PayIdPublicSuffixesTest(final ValidPublicTldTestVector validPublicTldTestVector) {
    this.validPublicTldTestVector = Objects.requireNonNull(validPublicTldTestVector);
  }

  /**
   * The data for this test...
   */
  @Parameters
  public static Collection<ValidPublicTldTestVector> data() throws URISyntaxException, MalformedURLException {
    final Scanner scanner;
    try {
      URL url = new URL("https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat");
      scanner = new Scanner(url.openStream());
      // read from your scanner
    } catch (IOException ex) {
      // there was some connection problem, or the file did not exist on the server,
      // or your URL was not in the right format.
      // think about what to do now, and put it here.
      throw new RuntimeException(ex.getMessage(), ex);
    }

    final Builder<ValidPublicTldTestVector> vectors = ImmutableList.builder();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (line != null && line.trim().length() != 0 && !line.startsWith("//")) {
        String host = HttpUrl.parse("https://example." + line.trim()).host();
        vectors.add(
          ValidPublicTldTestVector.builder()
            .tld(host)
            .expectedPayId("alice$" + line.trim())
            .build()
        );
      }
    }

    return vectors.build();
  }

  @Test
  public void testPublicTlds() {
    final PayId payId = PayId.of("payid:alice$" + validPublicTldTestVector.tld());

    assertThat(payId).isNotNull();
    assertThat(payId.account()).isEqualTo("alice");
    assertThat(payId.host()).isEqualTo(validPublicTldTestVector.tld());
    assertThat(payId.toString()).isEqualTo("payid:alice$" + validPublicTldTestVector.tld());
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableValidPublicTldTestVector.class)
  @JsonDeserialize(as = ImmutableValidPublicTldTestVector.class)
  public interface ValidPublicTldTestVector {

    static ImmutableValidPublicTldTestVector.Builder builder() {
      return ImmutableValidPublicTldTestVector.builder();
    }

    String tld();

    String expectedPayId();
  }
}
