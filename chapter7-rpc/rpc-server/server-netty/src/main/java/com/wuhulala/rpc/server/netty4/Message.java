package com.wuhulala.rpc.server.netty4;

import java.io.InputStream;

public class Message {
    private long id;
    private InputStream body;

    public Message(long id, InputStream body) {
        this.id = id;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream body) {
        this.body = body;
    }
}