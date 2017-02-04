package cache;

import org.springframework.beans.BeansException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.*;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * Created by BiColorCat on 2017/2/3.
 */
public class SpringRedisCacheManager extends RedisCacheManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public SpringRedisCacheManager(RedisOperations redisOperations) {
        super(redisOperations);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        parseCacheDuration(applicationContext);
    }

    private void parseCacheDuration(ApplicationContext applicationContext){
        Map<String,Long> cacheExpires = new HashMap<>();
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        Arrays.asList(beanNames).stream().forEach(beanName -> {
            Class clazz = applicationContext.getType(beanName);
            if(null == findAnnotation(clazz, Service.class)){
                return;
            }
            addCacheExpires(clazz, cacheExpires);
        });
        super.setExpires(cacheExpires);
    }

    private void addCacheExpires(final Class clazz, final Map<String, Long> cacheExpires) {
        ReflectionUtils.doWithMethods(clazz, (method) -> {
            ReflectionUtils.makeAccessible(method);
            CacheDuration cacheDuration = findAnnotation(method, CacheDuration.class);
            Cacheable cacheable = findAnnotation(method, Cacheable.class);
            if(cacheable != null){
                Set<String> cacheNames = new HashSet<>(Arrays.asList(cacheable.cacheNames()));
                cacheNames.stream().forEach(cacheName -> cacheExpires.put(cacheName,cacheDuration.duration()));
            }
        });
    }

}
