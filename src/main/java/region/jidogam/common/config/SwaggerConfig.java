package region.jidogam.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import region.jidogam.common.annotation.CurrentUserId;

@Configuration
public class SwaggerConfig {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        )
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .info(apiInfo());
  }

  private Info apiInfo() {
    return new Info()
        .title("지도감 Swagger")
        .description("지도감 REST API")
        .version("1.0.0");
  }

  @Bean
  public OperationCustomizer operationCustomizer() {
    return (operation, handlerMethod) -> {
      for (var parameter : handlerMethod.getMethodParameters()) {
        if (parameter.hasParameterAnnotation(CurrentUserId.class)) {
          String paramName = parameter.getParameterName();
          if (operation.getParameters() != null) {
            operation.getParameters().removeIf(p -> p.getName().equals(paramName));
          }
        }
      }
      return operation;
    };
  }
}