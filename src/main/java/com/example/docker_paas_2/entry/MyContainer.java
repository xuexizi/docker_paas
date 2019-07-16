package com.example.docker_paas_2.entry;

public class MyContainer {
    private String container_id;
    private String host_ip;
    private int host_port;
    private int container_port;

    public String getContainer_id() {
        return container_id;
    }

    public void setContainer_id(String container_id) {
        this.container_id = container_id;
    }

    public String getHost_ip() {
        return host_ip;
    }

    public void setHost_ip(String host_ip) {
        this.host_ip = host_ip;
    }

    public int getHost_port() {
        return host_port;
    }

    public void setHost_port(int host_port) {
        this.host_port = host_port;
    }

    public int getContainer_port() {
        return container_port;
    }

    public void setContainer_port(int container_port) {
        this.container_port = container_port;
    }

}
