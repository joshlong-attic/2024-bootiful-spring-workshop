package com.example.aot;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;

@SpringBootApplication
public class AotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AotApplication.class, args);
    }


}

class MessageRunner implements ApplicationRunner {

    private final Resource resource;

    MessageRunner(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var contents = this.resource.getContentAsString(Charset.defaultCharset());
        System.out.println("contents [" + contents + "]");
    }
}


class Foo {

    int x = 0;

    void bar() {
        System.out.println("invoking bar");
    }
}

class ReflectiveThingy {

    ReflectiveThingy() {

        var fooClass = Foo.class;
        for (var field : fooClass.getDeclaredFields())
            System.out.println("field [" + field + "]");

        for (var method : fooClass.getDeclaredMethods())
            System.out.println("method [" + method + "]");
    }
}

@Configuration
@ImportRuntimeHints(AotConfiguration.Hints.class)
//@RegisterReflectionForBinding(ReflectiveThingy.class)
class AotConfiguration {


    private static final Resource RESOURCE = new ClassPathResource("message");

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(Foo.class, MemberCategory.values());
            hints.resources().registerResource(RESOURCE);
        }
    }

    @Bean
    MessageRunner messageRunner() {
        return new MessageRunner(RESOURCE);
    }

    @Bean
    ReflectiveThingy reflectiveThingy() {
        return new ReflectiveThingy();
    }
}

