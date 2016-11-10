package money.fluid.ilp.connector.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.boot.Banner.Mode.CONSOLE;

@Configuration
@Import({
        EmbeddedServletContainerAutoConfiguration.EmbeddedTomcat.class,
        DispatcherServletAutoConfiguration.class,
        EmbeddedServletContainerAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        HttpEncodingAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        MessageSourceAutoConfiguration.class,
        ServerPropertiesAutoConfiguration.class,
        PropertyPlaceholderAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        //GoogleCloudConfig.class,
        //ServicesConfig.class,
        //FirebaseConfig.class,
        //PubsubConfig.class,
})
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"money.fluid.ilp.connector.controllers", "money.fluid.ilp.connector.filters"})
public class Application {
    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(CONSOLE);
        app.run(args);
    }
}