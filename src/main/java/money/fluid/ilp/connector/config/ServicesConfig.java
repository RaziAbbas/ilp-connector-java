package money.fluid.ilp.connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import money.fluid.ilp.connector.utils.ILPObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;

@Configuration
@ComponentScan(basePackages = {
        "money.fluid.ilp.connector.services",
})
public class ServicesConfig {

    @Bean
    ObjectMapper objectMapper() {
        return new ILPObjectMapper();
    }


    @Bean
    ExchangeRateProvider exchangeRateProvider() {
        return MonetaryConversions.getExchangeRateProvider();
    }
}