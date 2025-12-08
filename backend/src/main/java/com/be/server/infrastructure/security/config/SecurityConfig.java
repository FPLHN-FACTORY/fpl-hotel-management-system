package com.be.server.infrastructure.security.config;

// import com.be.server.infrastructure.constant.MappingConstants;
// import com.be.server.infrastructure.constant.Role;
// import com.be.server.infrastructure.exception.RestAuthenticationEntryPoint;
// import com.be.server.infrastructure.security.filter.TokenAuthenticationFilter;
// import com.be.server.infrastructure.security.oauth2.CustomOAuth2UserService;
// import com.be.server.infrastructure.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
// import com.be.server.infrastructure.security.oauth2.OAuth2AuthenticationFailureHandler;
// import com.be.server.infrastructure.security.oauth2.OAuth2AuthenticationSuccessHandler;
// import com.be.server.utils.Helper;
// import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.ProviderManager;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.BeanIds;
// import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.Collections;
// import java.util.List;

// @Configuration
// @EnableWebSecurity
// @EnableGlobalMethodSecurity(
//         securedEnabled = true,
//         jsr250Enabled = true,
//         prePostEnabled = true
// )
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final CustomOAuth2UserService customOAuth2UserService;

//     private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

//     private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

//     private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

//     @Value("${frontend.url}")
//     private String allowedOrigin;

//     @Bean
//     public TokenAuthenticationFilter tokenAuthenticationFilter() {
//         return new TokenAuthenticationFilter();
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean(BeanIds.AUTHENTICATION_MANAGER)
//     public AuthenticationManager authenticationManager(
//             PasswordEncoder passwordEncoder,
//             UserDetailsService userDetailsService
//     ) {
//         DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//         provider.setPasswordEncoder(passwordEncoder);
//         provider.setUserDetailsService(userDetailsService);
//         return new ProviderManager(provider);
//     }

//     @Bean
//     CorsConfigurationSource corsConfigurationSource() {
//         final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         CorsConfiguration config = new CorsConfiguration();
//         source.registerCorsConfiguration("/**", config.applyPermitDefaultValues());
//         config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "*"));
//         config.setAllowedOrigins(Collections.singletonList(allowedOrigin));
//         config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PUT", "OPTIONS", "PATCH", "DELETE"));
//         config.setAllowCredentials(true);
//         config.setExposedHeaders(List.of("Authorization"));
//         return source;
//     }

//     @Bean
//     protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http.csrf(AbstractHttpConfigurer::disable);
//         http.cors(c -> c.configurationSource(corsConfigurationSource()));
//         http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//         http.formLogin(AbstractHttpConfigurer::disable);
//         http.httpBasic(AbstractHttpConfigurer::disable);
//         http.exceptionHandling(e -> e.authenticationEntryPoint(new RestAuthenticationEntryPoint()));
//         http.authorizeHttpRequests(auth -> auth.requestMatchers(
//                         "/",
//                         "/error",
//                         "/favicon.ico",
//                         "/*/*.png",
//                         "/*/*.gif",
//                         "/*/*.svg",
//                         "/*/*.jpg",
//                         "/*/*.html",
//                         "/*/*.css",
//                         "/*/*.js"
//                 )
//                 .permitAll());
//         http.authorizeHttpRequests(
//                 auth -> auth.requestMatchers(
//                                 "/auth/**",
//                                 Helper.appendWildcard(MappingConstants.API_AUTH_PREFIX),
//                                 Helper.appendWildcard(MappingConstants.API_PERMITALL_PREFIX),
//                                 "/oauth2/**"
//                         )
//                         .permitAll()
//         );

//         http.authorizeHttpRequests(
//                 auth -> auth
//                         .requestMatchers(Helper.appendWildcard(MappingConstants.API_LE_TAN_PREFIX)).permitAll()
// //                        .requestMatchers(Helper.appendWildcard(MappingConstants.API_PERMITALL_PREFIX)).permitAll()

//         );

//         http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
//         http.oauth2Login(
//                 oauth2 -> oauth2.authorizationEndpoint(a -> a.baseUri("/oauth2/authorize"))
//                         .redirectionEndpoint(r -> r.baseUri("/oauth2/callback/**"))
//                         .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
//                         .authorizationEndpoint(a -> a.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
//                         .successHandler(oAuth2AuthenticationSuccessHandler)
//                         .failureHandler(oAuth2AuthenticationFailureHandler));
//         http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//         return http.build();
//     }
// }

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    // --- OAuth2 related fields commented out temporarily ---
    // private final CustomOAuth2UserService customOAuth2UserService;
    // private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    // private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    // private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Value("${frontend.url:}") // default empty string if not set
    private String allowedOrigin;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder,
                                                       org.springframework.security.core.userdetails.UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://10.*:*",
                "http://192.168.*:*",
                "http://localhost:*"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(c -> c.configurationSource(corsConfigurationSource()));
        http.sessionManagement(sess -> sess.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
        http.formLogin(fl -> fl.disable());
        http.httpBasic(hb -> hb.disable());
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        // --- OAuth2 and token filters commented out temporarily ---
        // http.oauth2Login(...);
        // http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
