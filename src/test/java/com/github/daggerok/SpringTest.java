package com.github.daggerok;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("spring framework tests")
public class SpringTest {

  private static Function<Throwable, RuntimeException> asRuntimeException =
      throwable -> Try.of(() -> new RuntimeException(throwable))
                      .onSuccess(e -> log.error(e.getCause().getLocalizedMessage()))
                      .get();

  @Test
  @DisplayName("should get classes recursively in given package")
  public void test() {
    val provider = new ClassPathScanningCandidateComponentProvider(/* useDefaultFilters */ false);
    Pattern recoursePattern = Pattern.compile(".*");
    provider.addIncludeFilter(new RegexPatternTypeFilter(recoursePattern));

    String basePackage = getClass().getPackage().getName();
    val classes = provider.findCandidateComponents(basePackage)
                          .parallelStream()
                          .map(BeanDefinition::getBeanClassName)
                          .map(s -> Try.of(() -> Class.forName(s))
                                       .getOrElseThrow(asRuntimeException))
                          .collect(Collectors.toList());

    assertThat(classes).hasSizeGreaterThan(4);
    classes.stream().map(String::valueOf).forEach(log::info);
  }
}
