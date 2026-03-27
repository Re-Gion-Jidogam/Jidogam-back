package region.jidogam.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.security.JidogamUserDetailsService;
import region.jidogam.infrastructure.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final JidogamUserDetailsService jidogamUserDetailsService;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AccessDeniedHandler accessDeniedHandler,
      AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // REST API

        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, PublicApiEndpoints.getPublicPostEndpoints())
            .permitAll()
            .requestMatchers(HttpMethod.GET, PublicApiEndpoints.getPublicGetEndpoints()).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
            .anyRequest().hasRole("USER"))

        .exceptionHandling(e -> e
            .accessDeniedHandler(accessDeniedHandler)
            .authenticationEntryPoint(authenticationEntryPoint))

        .addFilterBefore(
            new JwtAuthenticationFilter(jwtProvider, jidogamUserDetailsService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
