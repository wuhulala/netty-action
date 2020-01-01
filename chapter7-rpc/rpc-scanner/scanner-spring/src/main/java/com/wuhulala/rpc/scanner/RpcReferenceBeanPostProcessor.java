package com.wuhulala.rpc.scanner;

import com.wuhulala.rpc.annotation.RpcReference;
import com.wuhulala.rpc.scanner.bean.RpcReferenceBean;
import com.wuhulala.rpc.scanner.util.AnnotationUtils;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.core.annotation.AnnotationAttributes;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuhulala<br>
 * @date 2019/12/30<br>
 * @since v1.0<br>
 */
public class RpcReferenceBeanPostProcessor extends AnnotationInjectedBeanPostProcessor {

    public static final String BEAN_NAME = "rpcInner#RpcReferenceBeanPostProcessor";

    private Map<String, RpcReferenceBean> REFERENCE_BEAN_CACHE = new ConcurrentHashMap<>(32);

    public RpcReferenceBeanPostProcessor() {
        super(RpcReference.class);
    }

    @Override
    protected Object doGetInjectedBean(AnnotationAttributes attributes, Object bean, String beanName, Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) throws Exception {

        // 构造ReferenceBean
        String referencedBeanName = buildReferencedBeanName(attributes, injectedType);
        RpcReferenceBean referenceBean = buildReferencedBean(referencedBeanName, attributes, injectedType);
        referenceBean.init();
        // 获取ReferenceBean.getRef()
        Object ref = referenceBean.getRef();
        // 构造proxy
        return createProxy(ref, injectedType);
    }

    private RpcReferenceBean buildReferencedBean(String referenceBeanName, AnnotationAttributes attributes, Class<?> injectedType) {

        return Optional.ofNullable(REFERENCE_BEAN_CACHE.get(referenceBeanName)).orElseGet(() -> {
            RpcReferenceBean referenceBean = new RpcReferenceBean();
            referenceBean.setInterfaceClass(injectedType);
//            referenceBean.setGroup(attributes.getString("group"));
//            referenceBean.setVersion(attributes.getString("version"));
            REFERENCE_BEAN_CACHE.put(referenceBeanName, referenceBean);
            return referenceBean;
        });

    }

    private Object createProxy(Object ref, Class<?> serviceInterface) {
        return Proxy.newProxyInstance(getClassLoader(), new Class[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(ref, args);
            }
        });
    }

    @Override
    protected String buildInjectedObjectCacheKey(AnnotationAttributes attributes, Object bean, String beanName,
                                                 Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) {

        return buildReferencedBeanName(attributes, injectedType) +
                "#source=" + (injectedElement.getMember()) +
                "#attributes=" + AnnotationUtils.resolvePlaceholders(attributes, getEnvironment());
    }

    private String buildReferencedBeanName(AnnotationAttributes attributes, Class<?> injectedType) {

        ServiceBeanNameBuilder serviceBeanNameBuilder = ServiceBeanNameBuilder.create(attributes, injectedType, getEnvironment());

        return serviceBeanNameBuilder.build();
    }
}
