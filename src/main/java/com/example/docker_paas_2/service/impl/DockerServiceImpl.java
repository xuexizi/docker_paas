package com.example.docker_paas_2.service.impl;


import com.example.docker_paas_2.service.DockerService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.stereotype.Service;

@Service
public class DockerServiceImpl implements DockerService {

    //连接docker
    @Override
    public DockerClient connectDocker(String ip){

        String address = "tcp://" + ip + ":2375";
        return DockerClientBuilder.getInstance(address).build();

    }

    //创建exec bash
    @Override
    public String create_exec_bash(DockerClient dockerClient , String container_id){

        //create exec
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(container_id)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withTty(true)
                .withCmd("/bin/bash")
                .exec();

        return execCreateCmdResponse.getId();
    }
}
