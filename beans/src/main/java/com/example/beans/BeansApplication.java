package com.example.beans;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;

@EnableConfigurationProperties(BootifulProperties.class)
@SpringBootApplication
public class BeansApplication {

    public static void main(String[] args) throws BeansException {
//        var context = new ClassPathXmlApplicationContext("beans.xml");
//        context.start();

//        var context = new AnnotationConfigApplicationContext(BeansApplication.class);
//        context.start();

        var context = SpringApplication.run(BeansApplication.class, args);

    }

    @Bean
    Bar bar(Foo foo) {
        return new Bar(foo);
    }

    @Bean
    static BPP bpp() {
        return new BPP();
    }

    @Bean
    static BFPP bfpp() {
        return new BFPP();
    }

    @Bean
    static LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

}

record CustomerValidatedEvent(String name) {
}

@Component
class ValidationListener {

    @EventListener
    void onCustomerValidatedEvent(CustomerValidatedEvent event) {
        System.out.println("got a CustomerValidatedEvent: " + event);
    }
}

@Component
class ReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("the application is ready..");
    }
}

@Configuration
class PropertiesConfiguration {
    
    @Bean
    PropertiesRunner propertiesRunner(BootifulProperties properties) {
        return new PropertiesRunner(properties.name());
    }
}

class PropertiesRunner implements ApplicationRunner {

    private final String name;

    PropertiesRunner(String name) {
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("name: " + this.name);
    }
}

@Component
class Initializer implements ApplicationRunner {

    private final CustomerService customerService;

    Initializer(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.customerService.validate("foo");
    }
}

class LoggingInterceptor implements BeanPostProcessor {

    private final MI mi = new MI();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CustomerService) {
            var pf = new ProxyFactory(bean);
            pf.addAdvice(mi);
            pf.setProxyTargetClass(true);
            return pf.getProxy();
        }
        return bean;
    }
}

@Service
class CustomerService {

    private final ApplicationEventPublisher publisher;

    CustomerService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    boolean validate(String name) {
        System.out.println("isCustomer(" + name + ")");
        this.publisher.publishEvent(new CustomerValidatedEvent(name));
        return true;
    }
}

class MI implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("========================================================");
        System.out.println("invoking [" + invocation.getMethod().getName() +
                " with arguments [" + Arrays.toString(invocation.getArguments()) + "]");
        return invocation.proceed();
    }
}

class BFPP implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (var beanDefinitionName : beanFactory.getBeanDefinitionNames())
            System.out.println("beanDefinitionName [" + beanDefinitionName + ":" +
                    beanFactory.getBeanDefinition(beanDefinitionName).getBeanClassName() + "]");
    }
}

class BPP implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean [" + bean + "] beanName [" + beanName + "]");
        return bean;
    }
}

@Component
class Foo {
}

@ConfigurationProperties(prefix = "bootiful")
record BootifulProperties(String name) {
}

class Bar {

    private final Foo foo;

    Bar(Foo foo) {
        this.foo = foo;
        Assert.notNull(this.foo, "there should be a foo dependency");
        System.out.println("got a foo");
    }
}