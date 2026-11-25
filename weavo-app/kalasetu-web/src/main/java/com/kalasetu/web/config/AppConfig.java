package com.kalasetu.web.config;

import com.kalasetu.concurrency.BackgroundNotificationWorker;
import com.kalasetu.concurrency.ConcurrentOrderProcessor;
import com.kalasetu.fileio.CatalogFileService;
import com.kalasetu.payment.TestPaymentGatewayService;
import com.kalasetu.persistence.InMemoryMarketplaceRepository;
import com.kalasetu.persistence.MarketplaceRepository;
import com.kalasetu.persistence.SupabaseRestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean
    public MarketplaceRepository marketplaceRepository(
            @Value("${kalasetu.mode:auto}") String mode,
            @Value("${kalasetu.supabase-url:}") String supabaseUrl,
            @Value("${kalasetu.supabase-service-key:}") String serviceKey,
            @Value("${kalasetu.storage-bucket:product-images}") String bucket) {

        boolean hasSupabaseCreds = supabaseUrl != null && !supabaseUrl.isBlank()
                && serviceKey != null && !serviceKey.isBlank()
                && !supabaseUrl.contains("YOUR_PROJECT_REF");

        if (!"memory".equalsIgnoreCase(mode) && hasSupabaseCreds) {
            log.info("Initializing KalaSetu Repository in SUPABASE_CLOUD_POSTGRES mode ({})", supabaseUrl);
            return new SupabaseRestRepository(supabaseUrl, serviceKey, bucket);
        }
        log.info("Initializing KalaSetu Repository in IN_MEMORY_FALLBACK mode (configure SUPABASE_URL & SUPABASE_SERVICE_ROLE_KEY for live DB)");
        return new InMemoryMarketplaceRepository();
    }

    @Bean
    public TestPaymentGatewayService testPaymentGatewayService() {
        return new TestPaymentGatewayService();
    }

    @Bean(destroyMethod = "close")
    public BackgroundNotificationWorker backgroundNotificationWorker(MarketplaceRepository repository) {
        return new BackgroundNotificationWorker(repository);
    }

    @Bean(destroyMethod = "close")
    public ConcurrentOrderProcessor concurrentOrderProcessor(
            MarketplaceRepository repository,
            TestPaymentGatewayService gateway,
            BackgroundNotificationWorker worker) {
        return new ConcurrentOrderProcessor(repository, gateway, worker);
    }

    @Bean
    public CatalogFileService catalogFileService(@Value("${kalasetu.data-dir:./runtime-data}") String dataDir)
            throws IOException {
        return new CatalogFileService(Path.of(dataDir));
    }
}
