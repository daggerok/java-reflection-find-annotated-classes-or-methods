package com.github.daggerok;

import io.github.classgraph.ClassGraph;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("classgraph library tests")
public class ClassGraphTest {

  @Test
  @DisplayName("should get classes recursively in given package")
  public void test() {
    @Cleanup val scanResult = new ClassGraph().whitelistPackages(getClass().getPackage().getName())
                                              .enableClassInfo()
                                              .scan();
    List<Class<?>> classes = scanResult.getAllClasses().loadClasses();
    assertThat(classes).hasSizeGreaterThan(4);
    classes.stream().map(String::valueOf).forEach(log::info);
  }
}
