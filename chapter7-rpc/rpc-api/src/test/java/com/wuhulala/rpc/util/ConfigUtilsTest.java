package com.wuhulala.rpc.util;

import org.junit.Test;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuhulala<br>
 * @date 2020/1/4<br>
 * @since v1.0<br>
 */
public class ConfigUtilsTest {

    @Test
    public void getPropertyNamesWithPrefix() {
        ConfigUtils.getProperties();
        String prefix = "rpc.registry.instance.";
        List<String> registryPropNames= ConfigUtils.getPropertyNamesWithPrefix(prefix);



    }

}