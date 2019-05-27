package com.github.daggerok;

import com.google.common.reflect.ClassPath;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("java guava library tests")
public class GuavaTest {

  private static Function<Throwable, RuntimeException> asRuntimeException =
      throwable -> Try.of(() -> new RuntimeException(throwable))
                      .onSuccess(e -> log.error(e.getCause().getLocalizedMessage()))
                      .get();

  @Test
  @DisplayName("should get classes recursively in given package")
  public void test() {
    ClassPath classPath = Try.of(() -> ClassPath.from(getClass().getClassLoader()))
                             .getOrElseThrow(asRuntimeException);
    List<Class<?>> classes = classPath.getTopLevelClassesRecursive(getClass().getPackage().getName())
                                      .parallelStream()
                                      .map(ClassPath.ClassInfo::load)
                                      .collect(Collectors.toList());
    assertThat(classes).hasSizeGreaterThan(4);
    classes.stream().map(String::valueOf).forEach(log::info);
  }
}
