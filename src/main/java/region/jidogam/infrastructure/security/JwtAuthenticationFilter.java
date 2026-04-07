package region.jidogam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import region.jidogam.infrastructure.jwt.JwtProvider;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final JidogamUserDetailsService jidogamUserDetailsService;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ACCESS_TOKEN_COOKIE = "access_token";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = extractToken(request);

    if (token != null && jwtProvider.validateToken(token)) {

      UserDetails userDetails = jidogamUserDetailsService.loadUserByToken(token);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities()
          );

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    // 1. Authorization 헤더 우선
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }

    // 2. 쿠키 fallback (admin 페이지용)
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}
