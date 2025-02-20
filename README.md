# Winter - An IoC Container for Java

Winter is an Inversion of Control (IoC) framework for Java, inspired by Spring. It provides annotation-based dependency injection, bean lifecycle management, and a modular architecture for building scalable applications.

## Features

### 1. Dependency Injection (DI)
- **@Autowired**: Automatically injects dependencies into beans.
- **@Qualifier**: Allows specifying which bean to inject when multiple implementations exist.

### 2. Bean Management
- **@Component**: Marks a class as a managed bean.
- *(Future Release)* **@Bean**: Declares a bean definition inside a configuration class.
- *(Future Release)* **@Configuration**: Marks a class as a source of bean definitions.

### 3. Bean Lifecycle Hooks
- **@PostConstruct**: Runs initialization logic after dependency injection.
- **@PreDestroy**: Runs cleanup logic before the bean is destroyed.

### 4. Context Management
- **ApplicationContext**: Central interface for accessing beans and managing dependencies.
- **AnnotationApplicationContext**: Implementation that scans annotations for configuring the application context.

### 5. Bean Definition and Registration
- **BeanDefinition**: Metadata representation of a bean.
- **BeanFactory**: Core factory for managing bean instances.
- **BeanPostProcessor**: Allows custom modifications to beans after instantiation.
- **PackageScanner**: Scans packages to detect annotated components automatically.

### 6. Utility Support
- **AnnotationUtils**: Provides helper methods for annotation processing.
- **ClassUtils**: Assists with class path scanning and reflection.
- **PropertySource**: Manages externalized properties for configuration.

## Getting Started

### 1. Add Winter to Your Project
```xml
<dependency>
    <groupId>com.codeus</groupId>
    <artifactId>winter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Define a Bean
```java
@Component
public class MyService {
    public void execute() {
        System.out.println("Executing service...");
    }
}
```

### 3. Enable Dependency Injection *(Future Release)*
```java
@Configuration
public class AppConfig {
    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

### 4. Use the Application Context
```java
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationApplicationContext("packageToScan");
        MyService service = context.getBean(MyService.class);
        service.execute();
    }
}
```

## Future Enhancements
- Support for AOP (Aspect-Oriented Programming)
- Advanced property injection
- Configuration

## Contributing
Contributions are welcome! Feel free to submit issues and pull requests.

---
**Winter** - Bringing the chill to dependency injection!

Powered by **Codeus Community**.

