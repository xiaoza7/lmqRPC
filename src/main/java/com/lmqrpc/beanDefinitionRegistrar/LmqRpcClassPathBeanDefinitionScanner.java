package com.lmqrpc.beanDefinitionRegistrar;




import com.lmqrpc.entity.LmqRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.LinkedHashSet;
import java.util.Set;

/*
lmq
 */


public class LmqRpcClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner
{

    private ResourcePatternResolver resourcePatternResolver;
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


    protected final Logger logger = LoggerFactory.getLogger(LmqRpcClassPathBeanDefinitionScanner.class);

    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

    public LmqRpcClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }


    protected void registerFilters() {
        addIncludeFilter(new AnnotationTypeFilter(LmqRPC.class));
    }


    protected Set<BeanDefinition> doScanRpc(String... basePackages) {

        super.doScan(basePackages);

        Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
       try {
           for (String ss : basePackages) {
               String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                       resolveBasePackage(ss) + '/' + this.resourcePattern;
               Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
               for (Resource resource : resources) {
                   MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);

                       ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                 Class<?> c = Class.forName(sbd.getMetadata().getClassName());

                   System.out.println("ffff");
                   if( c.isAnnotationPresent(LmqRPC.class)) {
                          sbd.setResource(resource);
                          sbd.setSource(resource);
                          candidates.add(sbd);

                      }
               }
           }

       }catch (Exception e)
       {
          logger.info(e.getMessage());
       }

        return candidates;

    }

    private ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }
}