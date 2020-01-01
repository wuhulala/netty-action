package com.wuhulala.rpc.scanner;

import com.alibaba.cooma.ExtensionLoader;
import com.wuhulala.rpc.annotation.RpcService;
import com.wuhulala.rpc.scanner.bean.RpcServiceBean;
import com.wuhulala.rpc.bean.Invocation;
import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.scaner.ServiceScanner;
import com.wuhulala.rpc.scanner.util.AnnotationUtils;
import com.wuhulala.rpc.util.CollectionUtils;
import com.wuhulala.rpc.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;
import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.wuhulala.rpc.constants.CommonConstants.*;
import static com.wuhulala.rpc.constants.ConfigConstants.SERVICE_GROUP;
import static com.wuhulala.rpc.constants.ConfigConstants.SERVICE_VERSION;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @author wuhulala<br>
 * @date 2019/12/29<br>
 * @since v1.0<br>
 */
public class RpcServiceAnnotationBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware,
        ResourceLoaderAware, BeanClassLoaderAware, ServiceScanner, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RpcServiceAnnotationBeanPostProcessor.class);

    private BeanNameGenerator beanNameGenerator;


    private ClassLoader classLoader;

    private Environment environment;

    private ResourceLoader resourceLoader;

    private Set<String> packagesToScan;

    private Map<String, List<RpcDesc>> RPC_DESC_CACHE;

    private Map<String, RpcServiceBean> RPC_SERVICE_BEAN_CACHE;

    private ApplicationContext applicationContext;

    public RpcServiceAnnotationBeanPostProcessor(String... packagesToScan) {
        this(Arrays.asList(packagesToScan));
    }

    public RpcServiceAnnotationBeanPostProcessor(Collection<String> packagesToScan) {
        this(new LinkedHashSet<>(packagesToScan));
    }

    public RpcServiceAnnotationBeanPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
        beanNameGenerator = new AnnotationBeanNameGenerator();
        RPC_DESC_CACHE = new ConcurrentHashMap<>(128);
        RPC_SERVICE_BEAN_CACHE = new ConcurrentHashMap<>(128);
    }

    //----------  service scanner -----------------------//
    @Override
    public List<RpcDesc> scan(String packageName) {
        return RPC_DESC_CACHE.get(packageName);
    }

    @Override
    public <T> T getInvoker(Invocation invocation) {
        return (T) applicationContext.getBean(generateServiceBeanName(invocation), RpcServiceBean.class).getRef();
    }

    @PostConstruct
    public void registerToContainer() {
        logger.info("将当前SpringScanner注册到");
        ExtensionLoader.getExtensionLoader(ServiceScanner.class).registerExtension("spring", this);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        if (CollectionUtils.isEmpty(packagesToScan)) {
            logger.warn("待扫描的包竟然是空的，难以置信");
            return;
        }

        registerRpcServiceBean(packagesToScan, registry);

        registerToContainer();
    }

    private void registerRpcServiceBean(Set<String> packages, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
        packages.forEach(one -> registerOnePackage(one, scanner, registry));
    }

    private void registerOnePackage(String onePackage, ClassPathBeanDefinitionScanner scanner, BeanDefinitionRegistry registry) {
        // 1. 扫描@RpcService
        scanner.scan(onePackage);
        Set<BeanDefinitionHolder> beanDefinitionHolders = findAllBeanDefinition(scanner, onePackage, registry);
        // 2. 扫描注册RpcServiceBean为BeanDefinition
        registerOnePackageRpcServiceBean(onePackage, beanDefinitionHolders, registry);
        logger.info("注册了{}个RpcServiceBean。", beanDefinitionHolders.size());
    }

    private void registerOnePackageRpcServiceBean(String onePackage, Set<BeanDefinitionHolder> beanDefinitionHolders, BeanDefinitionRegistry registry) {
        beanDefinitionHolders.forEach(beanDefinitionHolder -> {
            Class<?> beanClass = resolveClass(beanDefinitionHolder);

            Annotation service = findServiceAnnotation(beanClass);

            AnnotationAttributes serviceAnnotationAttributes = getAnnotationAttributes(service, false, false);

            Class<?> interfaceClass = AnnotationUtils.resolveServiceInterfaceClass(serviceAnnotationAttributes, beanClass);
            String rpcServiceBeanName = generateServiceBeanName(serviceAnnotationAttributes, interfaceClass);

            AbstractBeanDefinition bd = buildBeanDefinition(beanDefinitionHolder.getBeanName(), interfaceClass, service, serviceAnnotationAttributes);
            registry.registerBeanDefinition(rpcServiceBeanName, bd);


            scanRpcDesc(onePackage, beanClass);
            logger.info("注册了Bean#{} 对应的 RpcServiceBean {}", beanDefinitionHolder.getBeanName(), rpcServiceBeanName);
        });
    }

    private void scanRpcDesc(String onePackage, Class<?> beanClass) {
        synchronized (this) {
            Set<RpcDesc> rpcDescs = parseRpcDesc(beanClass);
            Optional.ofNullable(RPC_DESC_CACHE.get(onePackage)).orElseGet(()->{
                    List<RpcDesc> list = new LinkedList();
                    RPC_DESC_CACHE.put(onePackage, list);
                    return list;
            }).addAll(rpcDescs);
            logger.info("从{} 中扫描 {} 个RpcDesc", beanClass, rpcDescs.size());
        }
    }

    private AbstractBeanDefinition buildBeanDefinition(String refBeanName, Class<?> beanClass, Annotation service, AnnotationAttributes serviceAnnotationAttributes) {
        return rootBeanDefinition(RpcServiceBean.class)
                .addPropertyValue("ref", new RuntimeBeanReference(refBeanName))
                .addPropertyValue("interfaceName", beanClass.getName())
                .addPropertyValue("group", getGroup(serviceAnnotationAttributes))
                .addPropertyValue("version", getVersion(serviceAnnotationAttributes))
                .getBeanDefinition();
    }

    private Annotation findServiceAnnotation(Class<?> beanClass) {
        return beanClass.getAnnotation(RpcService.class);
    }


    private Set<BeanDefinitionHolder> findAllBeanDefinition(ClassPathBeanDefinitionScanner scanner, String onePackage, BeanDefinitionRegistry registry) {
        return scanner.findCandidateComponents(onePackage).stream().map(beanDefinition -> {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            return new BeanDefinitionHolder(beanDefinition, beanName);
        }).collect(Collectors.toSet());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return resolveClass(beanDefinition);
    }

    private Class<?> resolveClass(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        assert beanClassName != null;
        return resolveClassName(beanClassName, classLoader);
    }

    private String generateServiceBeanName(AnnotationAttributes serviceAnnotationAttributes, Class<?> interfaceClass) {
        ServiceBeanNameBuilder builder = ServiceBeanNameBuilder.create(interfaceClass, environment)
                .group(getGroup(serviceAnnotationAttributes))
                .version(getVersion(serviceAnnotationAttributes));
        return builder.build();
    }

    private String generateServiceBeanName(Invocation inv) {
        ServiceBeanNameBuilder builder = ServiceBeanNameBuilder.create(inv.getServiceClass(), environment)
                .group(inv.getAttachments().getOrDefault("group", ConfigUtils.getProperty(SERVICE_GROUP)))
                .version(inv.getAttachments().getOrDefault("version", ConfigUtils.getProperty(SERVICE_VERSION)));
        return builder.build();
    }

    private String getVersion(AnnotationAttributes serviceAnnotationAttributes) {
        String version = serviceAnnotationAttributes.getString("version");
        return StringUtils.isEmpty(version) ? ConfigUtils.getProperty(SERVICE_VERSION) : version;
    }

    private String getGroup(AnnotationAttributes serviceAnnotationAttributes) {
        String group = serviceAnnotationAttributes.getString("group");
        return StringUtils.isEmpty(group) ? ConfigUtils.getProperty(SERVICE_GROUP) : group;
    }

    private Set<RpcDesc> parseRpcDesc(Class<?> clazz) {
        Set<RpcDesc> set = new LinkedHashSet<>();
        Class<?>[] interfaces = clazz.getInterfaces();
        RpcService rpcService = clazz.getAnnotation(RpcService.class);
        RpcDesc desc = null;
        String[] protocols = rpcService.protocol();
        if (protocols.length == 0) {
            protocols = new String[]{ConfigUtils.getProperty("rpc.protocol")};
        }
        String host = ConfigUtils.getProperty("rpc.host");
        int port = Integer.parseInt(ConfigUtils.getProperty("rpc.port", "9001"));
        for (Class<?> interfaceClazz : interfaces) {
            for (String protocol : protocols) {
                String interfaceName = interfaceClazz.getName();
                desc = new RpcDesc(protocol, host, port)
                        .addParameter(INTERFACE_KEY, interfaceName)
                        .addParameter(GROUP_KEY, ConfigUtils.getProperty("rpc.group", "default"))
                        .addParameter(VERSION_KEY, ConfigUtils.getProperty("rpc.version", "v"));
                set.add(desc);
            }
        }
        return set;
    }
}
