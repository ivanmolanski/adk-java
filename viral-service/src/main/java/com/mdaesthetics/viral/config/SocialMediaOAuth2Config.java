package com.mdaesthetics.viral.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SocialMediaOAuth2Config {

    @Value("${social.tiktok.client-id:}")
    private String tiktokClientId;
    
    @Value("${social.tiktok.client-secret:}")
    private String tiktokClientSecret;
    
    @Value("${social.instagram.client-id:}")
    private String instagramClientId;
    
    @Value("${social.instagram.client-secret:}")
    private String instagramClientSecret;
    
    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/health", "/actuator/**", "/auth/**").permitAll()
                .requestMatchers("/analyze", "/content/**", "/pipeline/**", "/qa/**").permitAll()
                .requestMatchers("/", "/login", "/oauth2/**", "/dashboard", "/chat").permitAll()
                .requestMatchers("/api/viral/**", "/api/analyze/**", "/api/content/**").permitAll()
                .requestMatchers("/api/social/connect/**", "/api/social/post/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
            );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            tiktokClientRegistration(),
            instagramClientRegistration()
        );
    }

    private ClientRegistration tiktokClientRegistration() {
        return ClientRegistration.withRegistrationId("tiktok")
            .clientId(tiktokClientId)
            .clientSecret(tiktokClientSecret)
            .scope("user.info.basic", "video.upload", "video.publish")
            .authorizationUri("https://www.tiktok.com/v2/auth/authorize/")
            .tokenUri("https://open.tiktokapis.com/v2/oauth/token/")
            .userInfoUri("https://open.tiktokapis.com/v2/user/info/")
            .userNameAttributeName("data.user.display_name")
            .clientName("TikTok")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .build();
    }

    private ClientRegistration instagramClientRegistration() {
        return ClientRegistration.withRegistrationId("instagram")
            .clientId(instagramClientId)
            .clientSecret(instagramClientSecret)
            .scope("user_profile", "user_media")
            .authorizationUri("https://api.instagram.com/oauth/authorize")
            .tokenUri("https://api.instagram.com/oauth/access_token")
            .userInfoUri("https://graph.instagram.com/me")
            .userNameAttributeName("username")
            .clientName("Instagram")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = 
            OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = 
            new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, 
                authorizedClientRepository);
        
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}