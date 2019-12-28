package com.wuhulala.rpc.server.netty4;

import com.wuhulala.rpc.serialzation.Cleanable;
import com.wuhulala.rpc.serialzation.ObjectInput;
import com.wuhulala.rpc.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.wuhulala.rpc.constants.CommonConstants.*;

public class Request {

    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];


    private long id;

    private String dubboVersion;

    private long requestId;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private Map<String, String> attachments;

    private InputStream body;

    private ObjectInput in;

    public Request(long id, ObjectInput in) {
        this.id = id;
        this.in = in;
    }

    public Request decode() throws IOException {

        String dubboVersion = in.readUTF();
        setDubboVersion(dubboVersion);
        setAttachment(DUBBO_VERSION_KEY, dubboVersion);

        setAttachment(PATH_KEY, in.readUTF());

        setAttachment(VERSION_KEY, in.readUTF());

        setMethodName(in.readUTF());
        try {
            // 读取参数类型
            Object[] args;
            Class<?>[] pts;
            String desc = in.readUTF();
            if (desc.length() == 0) {
                pts = EMPTY_CLASS_ARRAY;
                args = EMPTY_OBJECT_ARRAY;
            } else {
                pts = ReflectUtils.desc2classArray(desc);
                args = new Object[pts.length];
                for (int i = 0; i < args.length; i++) {
                    try {
                        args[i] = in.readObject(pts[i]);
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Decode argument failed: " + e.getMessage(), e);
                        }
                    }
                }
            }
            setParameterTypes(pts);

            Map<String, String> map = (Map<String, String>) in.readObject(Map.class);
            if (map != null && map.size() > 0) {
                Map<String, String> attachment = getAttachments();
                if (attachment == null) {
                    attachment = new HashMap<String, String>();
                }
                attachment.putAll(map);
                setAttachments(attachment);
            }

//            //读取参数 ,may be callback
//            for (int i = 0; i < args.length; i++) {
//                args[i] = decodeInvocationArgument(channel, this, pts, i, args[i]);
//            }
            //
            setArguments(args);

        } catch (ClassNotFoundException e) {
            throw new IOException("Read invocation data failed.", e);
        } finally {
            if (in instanceof Cleanable) {
                ((Cleanable) in).cleanup();
            }
        }
        return this;
    }

    public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<String, String>();
        }
        attachments.put(key, value);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream body) {
        this.body = body;
    }

    public String getDubboVersion() {
        return dubboVersion;
    }

    public void setDubboVersion(String dubboVersion) {
        this.dubboVersion = dubboVersion;
    }

    public String getClassName(){
        return attachments.get(PATH_KEY);
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestId=" + requestId +
                ", dubboVersion='" + dubboVersion + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", arguments=" + Arrays.toString(arguments) +
                ", attachments=" + attachments +
                '}';
    }
}