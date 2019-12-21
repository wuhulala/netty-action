import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.pojo.Cluster;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
public class NacosTest {

    public static void main(String[] args) throws NacosException {
        NamingService naming = NamingFactory.createNamingService("192.168.1.101:8848");
        naming.registerInstance("nacos.test.3", "11.11.11.11", 8888, "TEST1");

        Instance instance = new Instance();
        instance.setIp("55.55.55.55");
        instance.setPort(9999);
        instance.setHealthy(false);
        instance.setWeight(2.0);
        Map<String, String> instanceMeta = new HashMap<>();
        instanceMeta.put("site", "et2");
        instance.setMetadata(instanceMeta);

        Service service = new Service("nacos.test.4");
        service.setAppName("nacos-naming");
//        service.sethealthCheckMode("server");
//        service.setEnableHealthCheck(true);
        service.setProtectThreshold(0.8F);
        service.setGroupName("CNCF");
        Map<String, String> serviceMeta = new HashMap<>();
        serviceMeta.put("symmetricCall", "true");
        service.setMetadata(serviceMeta);
        instance.setServiceName(service.getName());

        Cluster cluster = new Cluster();
        cluster.setName("TEST5");
        AbstractHealthChecker.Http healthChecker = new AbstractHealthChecker.Http();
        healthChecker.setExpectedResponseCode(400);
//        healthChecker.setCurlHost("USer-Agent|Nacos");
//        healthChecker.setCurlPath("/xxx.html");
        cluster.setHealthChecker(healthChecker);
        Map<String, String> clusterMeta = new HashMap<>();
        clusterMeta.put("xxx", "yyyy");
        cluster.setMetadata(clusterMeta);

        instance.setClusterName(cluster.getName());

        naming.registerInstance("nacos.test.4", instance);
    }


}
