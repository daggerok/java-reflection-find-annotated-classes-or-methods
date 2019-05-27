package com.github.daggerok;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ReflectionsLibraryUsageTest {

  private static Function<Throwable, RuntimeException> asRuntimeException =
      throwable -> Try.of(() -> new RuntimeException(throwable))
                      .onSuccess(e -> log.error(e.getCause().getLocalizedMessage()))
                      .get();

  @Test
  public void java_vintage_test_from_java_test_sources() {
    val classes = new Reflections(getClass(),
                                  new SubTypesScanner(false)).getAllTypes()
                                                             .parallelStream()
                                                             .peek(log::info)
                                                             .map(s -> Try.of(() -> Class.forName(s))
                                                                          .getOrElseThrow(asRuntimeException))
                                                             .collect(Collectors.toList());
    assertThat(classes).hasSizeGreaterThan(4);
  }
}
