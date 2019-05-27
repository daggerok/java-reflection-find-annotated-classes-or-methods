package com.github.daggerok;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("java reflections library tests")
public class ReflectionsLibraryTest {

  private static Function<Throwable, RuntimeException> asRuntimeException =
      throwable -> Try.of(() -> new RuntimeException(throwable))
                      .onSuccess(e -> log.error(e.getCause().getLocalizedMessage()))
                      .get();

  @Test
  @DisplayName("should get classes recursively in given package")
  public void test() {
    val classes = new Reflections(getClass().getPackage().getName(),
                                  new SubTypesScanner(false)).getAllTypes()
                                                             .parallelStream()
                                                             .map(s -> Try.of(() -> Class.forName(s)).getOrElseThrow(asRuntimeException))
                                                             .collect(Collectors.toList());
    assertThat(classes).hasSizeGreaterThan(4);
    classes.stream().map(String::valueOf).forEach(log::info);
  }
}
