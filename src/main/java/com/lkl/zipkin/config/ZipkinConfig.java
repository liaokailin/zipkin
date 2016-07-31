package com.lkl.zipkin.config;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.okhttp.BraveOkHttpRequestResponseInterceptor;
import com.github.kristofa.brave.servlet.BraveServletFilter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by liaokailin on 16/7/27.
 */
@Configuration
public class ZipkinConfig {

    @Autowired
    private ZipkinProperties properties;


    @Bean
    public SpanCollector spanCollector() {
        HttpSpanCollector.Config config = HttpSpanCollector.Config.builder().connectTimeout(properties.getConnectTimeout()).readTimeout(properties.getReadTimeout())
                .compressionEnabled(properties.isCompressionEnabled()).flushInterval(properties.getFlushInterval()).build();
        return HttpSpanCollector.create(properties.getUrl(), config, new EmptySpanCollectorMetricsHandler());
    }


    @Bean
    public Brave brave(SpanCollector spanCollector){
        Brave.Builder builder = new Brave.Builder(properties.getServiceName());  //指定state
        builder.spanCollector(spanCollector);
        builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        Brave brave = builder.build();
        return brave;
    }

    @Bean
    public BraveServletFilter braveServletFilter(Brave brave){
        BraveServletFilter filter = new BraveServletFilter(brave.serverRequestInterceptor(),brave.serverResponseInterceptor(),new DefaultSpanNameProvider());
        return filter;
    }

    @Bean
    public OkHttpClient okHttpClient(Brave brave){
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BraveOkHttpRequestResponseInterceptor(brave.clientRequestInterceptor(), brave.clientResponseInterceptor(), new DefaultSpanNameProvider()))
                .build();
        return client;
    }
}
