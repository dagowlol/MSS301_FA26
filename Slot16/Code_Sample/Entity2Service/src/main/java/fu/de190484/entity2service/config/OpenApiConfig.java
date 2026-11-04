package fu.de190484.entity2service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI entity2OpenApi() {
        return new OpenAPI().info(new Info()
                .title("Entity2 Service API")
                .version("1.0.0")
                .description("MSS301 Practical Exam - Service 2"));
    }
}
