package com.github.daggerok;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("java plain reflection tests")
class PlainJavaReflectionTest {

  private static final Function<Throwable, RuntimeException> asRuntimeException = throwable -> {
    log.error(throwable.getLocalizedMessage());
    return new RuntimeException(throwable);
  };

  /*private static final Supplier<RuntimeException> andThenJustDie = () -> {
    log.error("just dei...");
    return new RuntimeException();
  };*/

  private static final Function<String, Collection<Class<?>>> findAllPackageClasses = basePackageName -> {

    Locale locale = Locale.getDefault();
    Charset charset = StandardCharsets.UTF_8;
    val fileManager = ToolProvider.getSystemJavaCompiler()
                                  .getStandardFileManager(/* diagnosticListener */ null, locale, charset);

    StandardLocation location = StandardLocation.CLASS_PATH;
    JavaFileObject.Kind kind = JavaFileObject.Kind.CLASS;
    Set<JavaFileObject.Kind> kinds = Collections.singleton(kind);
    val javaFileObjects = Try.of(() -> fileManager.list(location, basePackageName, kinds, /* recurse */ true))
                             .getOrElseThrow(asRuntimeException);

    String pathToPackageAndClass = basePackageName.replace(".", File.separator);
    Function<String, String> mapToClassName = s -> {
      String prefix = Arrays.stream(s.split(pathToPackageAndClass))
                            .findFirst()
                            .orElse("");
                            //.orElseThrow(andThenJustDie);
      return s.replaceFirst(prefix, "")
              .replaceAll(File.separator, ".");
    };

    return StreamSupport.stream(javaFileObjects.spliterator(), /* parallel */ true)
                        .filter(javaFileObject -> javaFileObject.getKind().equals(kind))
                        .map(FileObject::getName)
                        .map(fileObjectName -> fileObjectName.replace(".class", ""))
                        .map(mapToClassName)
                        .map(className -> Try.of(() -> Class.forName(className))
                                             .getOrElseThrow(asRuntimeException))
                        .collect(Collectors.toList());
  };

  @Test
  @DisplayName("should get classes recursively in given package")
  void test() {
    Collection<Class<?>> classes = findAllPackageClasses.apply(getClass().getPackage().getName());
    assertThat(classes).hasSizeGreaterThan(4);
    classes.stream().map(String::valueOf).forEach(log::info);
  }
}
