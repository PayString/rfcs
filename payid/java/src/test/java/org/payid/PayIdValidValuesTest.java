package org.payid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.payid.AbstractPayId.upperCasePercentEncoded;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.immutables.value.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Unit tests for {@link PayId}.
 */
@RunWith(Parameterized.class)
public class PayIdValidValuesTest {

  private ValidPayIdTestVector validPayIdTestVector;

  public PayIdValidValuesTest(final ValidPayIdTestVector validPayIdTestVector) {
    this.validPayIdTestVector = Objects.requireNonNull(validPayIdTestVector);
  }

  /**
   * The data for this test...
   */
  @Parameters
  public static Collection<ValidPayIdTestVector> data() throws URISyntaxException {
    final URI baseUri = PayIdValidValuesTest.class.getResource(
      PayIdValidValuesTest.class.getSimpleName() + ".class"
    ).toURI();
    final File baseDirectoryFile = new File(baseUri).getParentFile();
    final File validTestVectorDir = new File(baseDirectoryFile, "");
    final Builder<ValidPayIdTestVector> vectors = ImmutableList.builder();
    final ObjectMapper mapper = new ObjectMapper();

    Arrays.stream(validTestVectorDir.listFiles()).forEach(file -> {
      try {
        if (file.getName().endsWith("valid-payids.json")) {
          final List<ValidPayIdTestVector> testVectors = mapper.readValue(file,
            new TypeReference<List<ValidPayIdTestVector>>() {
            });

          vectors.addAll(testVectors.stream()
            .map(tv -> ValidPayIdTestVector.builder()
              .description(tv.description())
              .payIdInput(tv.payIdInput())
              .expectedAccountId(tv.expectedAccountId())
              .expectedHost(tv.expectedHost())
              .expectedStringValue(tv.expectedStringValue())
              .build()
            )
            .collect(Collectors.toList()));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return vectors.build();
  }

  @Test
  public void testValidValues() {
    final PayId payId = PayId.of(validPayIdTestVector.payIdInput());

    assertThat(payId).isNotNull();
    assertThat(payId.account()).isEqualTo(validPayIdTestVector.expectedAccountId());
    assertThat(payId.host()).isEqualTo(validPayIdTestVector.expectedHost());
    assertThat(payId.toString()).isEqualTo(validPayIdTestVector.expectedStringValue());
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

  @Value.Immutable
  @JsonSerialize(as = ImmutableValidPayIdTestVector.class)
  @JsonDeserialize(as = ImmutableValidPayIdTestVector.class)
  public interface ValidPayIdTestVector {

    static ImmutableValidPayIdTestVector.Builder builder() {
      return ImmutableValidPayIdTestVector.builder();
    }

    String description();

    String payIdInput();

    String expectedAccountId();

    String expectedHost();

    String expectedStringValue();

  }
}
