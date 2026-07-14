package io.roadmap.filestorage.configs;

import io.minio.MinioClient;
import io.roadmap.filestorage.intecrceptors.DecodeParamsInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {
    private final DecodeParamsInterceptor decodeParamsinterceptor;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("minio", 9000, false)
                .credentials("minioadmin", "minioadmin")
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(decodeParamsinterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**");
    }
}
