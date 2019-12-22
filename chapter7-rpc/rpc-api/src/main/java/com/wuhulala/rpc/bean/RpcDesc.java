package com.wuhulala.rpc.bean;

import com.wuhulala.rpc.util.CollectionUtils;
import com.wuhulala.rpc.util.NetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

import static com.wuhulala.rpc.constants.CommonConstants.*;

/**
 * RPC 描述符
 * <p>
 * http://username:password@10.20.130.230:8080/list?version=1.0.0
 *
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
public class RpcDesc implements Serializable {

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    public static final String DEFAULT_KEY_PREFIX = "default.";

    /**
     * 协议名称
     */
    private String protocol;

    private String username;

    private String password;

    private String host;

    private int port;

    private String path;

    private Class<?> type;


    // CACHE
    private volatile transient String ip;

    private volatile transient String full;

    private volatile transient String identity;

    private volatile transient String parameter;

    private volatile transient String string;


    private final Map<String, String> parameters;

    public RpcDesc(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public RpcDesc(String protocol, String host, int port, String[] pairs) { // varargs ... conflict with the following path argument, use array instead.
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public RpcDesc(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public RpcDesc(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public RpcDesc(String protocol, String host, int port, String path, String... pairs) {
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public RpcDesc(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public RpcDesc(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public RpcDesc(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public RpcDesc(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if (StringUtils.isEmpty(username)
                && StringUtils.isNotEmpty(password)) {
            throw new IllegalArgumentException("Invalid ServiceBean, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        // trim the beginning "/"
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<>();
        } else {
            parameters = new HashMap<>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public String getParameter(String key) {
        String value = parameters.get(key);
        return StringUtils.isEmpty(value) ? parameters.get(DEFAULT_KEY_PREFIX + key) : value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        return StringUtils.isEmpty(value) ? defaultValue : COMMA_SPLIT_PATTERN.split(value);
    }

    public List<String> getParameter(String key, List<String> defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        String[] strArray = COMMA_SPLIT_PATTERN.split(value);
        return Arrays.asList(strArray);
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * Parse RpcDesc string
     *
     * @param RpcDesc RpcDesc string
     * @return RpcDesc instance
     * @see RpcDesc
     */
    public static RpcDesc valueOf(String RpcDesc) {
        if (RpcDesc == null || (RpcDesc = RpcDesc.trim()).length() == 0) {
            throw new IllegalArgumentException("RpcDesc == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = RpcDesc.indexOf("?"); // separator between body and parameters
        if (i >= 0) {
            String[] parts = RpcDesc.substring(i + 1).split("&");
            parameters = new HashMap<>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            RpcDesc = RpcDesc.substring(0, i);
        }
        i = RpcDesc.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("RpcDesc missing protocol: \"" + RpcDesc + "\"");
            }
            protocol = RpcDesc.substring(0, i);
            RpcDesc = RpcDesc.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = RpcDesc.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("RpcDesc missing protocol: \"" + RpcDesc + "\"");
                }
                protocol = RpcDesc.substring(0, i);
                RpcDesc = RpcDesc.substring(i + 1);
            }
        }

        i = RpcDesc.indexOf("/");
        if (i >= 0) {
            path = RpcDesc.substring(i + 1);
            RpcDesc = RpcDesc.substring(0, i);
        }
        i = RpcDesc.lastIndexOf("@");
        if (i >= 0) {
            username = RpcDesc.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            RpcDesc = RpcDesc.substring(i + 1);
        }
        i = RpcDesc.lastIndexOf(":");
        if (i >= 0 && i < RpcDesc.length() - 1) {
            if (RpcDesc.lastIndexOf("%") > i) {
                // ipv6 address with scope id
                // e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
                // see https://howdoesinternetwork.com/2013/ipv6-zone-id
                // ignore
            } else {
                port = Integer.parseInt(RpcDesc.substring(i + 1));
                RpcDesc = RpcDesc.substring(0, i);
            }
        }
        if (RpcDesc.length() > 0) {
            host = RpcDesc;
        }
        return new RpcDesc(protocol, username, password, host, port, path, parameters);
    }

    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        return string = buildString(false, true); // no show username and password
    }

    public String toString(String... parameters) {
        return buildString(false, true, parameters); // no show username and password
    }

    public String toIdentityString() {
        if (identity != null) {
            return identity;
        }
        return identity = buildString(true, false); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toIdentityString(String... parameters) {
        return buildString(true, false, parameters); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toFullString() {
        if (full != null) {
            return full;
        }
        return full = buildString(true, true);
    }

    public String toFullString(String... parameters) {
        return buildString(true, true, parameters);
    }

    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (CollectionUtils.isNotEmptyMap(getParameters())) {
            List<String> includes = (ArrayUtils.isEmpty(parameters) ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                        && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (StringUtils.isNotEmpty(protocol)) {
            buf.append(protocol);
            buf.append("://");
        }
        if (appendUser && StringUtils.isNotEmpty(username)) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }

        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public String getServiceKey() {
        String inf = getServiceInterface();
        if (inf == null) {
            return null;
        }
        return buildKey(inf, getParameter(GROUP_KEY), getParameter(VERSION_KEY));
    }

    /**
     * The format of return value is '{group}/{path/interfaceName}:{version}'
     * @return
     */
    public String getPathKey() {
        String inf = StringUtils.isNotEmpty(path) ? path : getServiceInterface();
        if (inf == null) {
            return null;
        }
        return buildKey(inf, getParameter("group"), getParameter("version"));
    }

    public static String buildKey(String path, String group, String version) {
        StringBuilder buf = new StringBuilder();
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(path);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public String getServiceInterface() {
        return getParameter("interface", path);
    }

    public RpcDesc addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, Enum<?> value) {
        if (value == null) {
            return this;
        }
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, Number value) {
        if (value == null) {
            return this;
        }
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return addParameter(key, String.valueOf(value));
    }

    public RpcDesc addParameter(String key, String value) {
        if (StringUtils.isEmpty(key)
                || StringUtils.isEmpty(value)) {
            return this;
        }
        // if value doesn't change, return immediately
        if (value.equals(getParameters().get(key))) { // value != null
            return this;
        }

        Map<String, String> map = new HashMap<>(getParameters());
        map.put(key, value);
        return new RpcDesc(protocol, username, password, host, port, path, map);
    }

    public RpcDesc addParameterIfAbsent(String key, String value) {
        if (StringUtils.isEmpty(key)
                || StringUtils.isEmpty(value)) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<>(getParameters());
        map.put(key, value);
        return new RpcDesc(protocol, username, password, host, port, path, map);
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    /**
     * Add parameters to a new RpcDesc.
     *
     * @param parameters parameters in key-value pairs
     * @return A new RpcDesc
     */
    public RpcDesc addParameters(Map<String, String> parameters) {
        if (CollectionUtils.isEmptyMap(parameters)) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = getParameters().get(entry.getKey());
            if (value == null) {
                if (entry.getValue() != null) {
                    hasAndEqual = false;
                    break;
                }
            } else {
                if (!value.equals(entry.getValue())) {
                    hasAndEqual = false;
                    break;
                }
            }
        }
        // return immediately if there's no change
        if (hasAndEqual) {
            return this;
        }

        Map<String, String> map = new HashMap<>(getParameters());
        map.putAll(parameters);
        return new RpcDesc(protocol, username, password, host, port, path, map);
    }

    public RpcDesc addParametersIfAbsent(Map<String, String> parameters) {
        if (CollectionUtils.isEmptyMap(parameters)) {
            return this;
        }
        Map<String, String> map = new HashMap<>(parameters);
        map.putAll(getParameters());
        return new RpcDesc(protocol, username, password, host, port, path, map);
    }

    public RpcDesc addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }
//
//    public RpcDesc addParameterString(String query) {
//        if (StringUtils.isEmpty(query)) {
//            return this;
//        }
//        return addParameters(StringUtils.parseQueryString(query));
//    }

    /**
     * The format is "{interface}:[version]:[group]"
     * @return
     */
    public String getColonSeparatedKey() {
        StringBuilder serviceNameBuilder = new StringBuilder();
        append(serviceNameBuilder, INTERFACE_KEY, true);
        append(serviceNameBuilder, VERSION_KEY, false);
        append(serviceNameBuilder, GROUP_KEY, false);
        return serviceNameBuilder.toString();
    }

    private void append(StringBuilder target, String parameterName, boolean first) {
        String parameterValue = this.getParameter(parameterName);
        if (!StringUtils.isBlank(parameterValue)) {
            if (!first) {
                target.append(":");
            }
            target.append(parameterValue);
        } else {
            target.append(":");
        }
    }

    public RpcDesc removeParameter(String key) {
        if (StringUtils.isEmpty(key)) {
            return this;
        }
        return removeParameters(key);
    }

    public RpcDesc removeParameters(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    public RpcDesc removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new RpcDesc(protocol, username, password, host, port, path, map);
    }

    public RpcDesc clearParameters() {
        return new RpcDesc(protocol, username, password, host, port, path, new HashMap<>());
    }

}
