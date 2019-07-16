package com.example.docker_paas_2.service;

import com.github.dockerjava.api.DockerClient;


public interface DockerService {



    //连接docker
    public DockerClient connectDocker(String ip);

    //创建exec bash
    public String create_exec_bash(DockerClient dockerClient, String container_id);

}
