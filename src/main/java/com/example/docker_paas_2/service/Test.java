package com.example.docker_paas_2.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateServiceResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.io.*;
import java.util.*;

import java.io.UnsupportedEncodingException;

public class Test {

    private static void testCreateService(String service_name , String image_name) throws DockerException, InterruptedException {
        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://192.168.100.109:2375").build();

        dockerClient.pullImageCmd("nginx").withTag("v1").withRegistry("jkxxz/").exec(new PullImageResultCallback());

//        String image_name = "jkxxz/nginx:v1";
        dockerClient.pullImageCmd(image_name)
                .exec(new PullImageResultCallback())
                .awaitCompletion();

//        dockerClient.pullImageCmd("busybox")
//                .withTag("latest")
//                .exec(new PullImageResultCallback());

//        dockerClient.removeImageCmd(imageId)
//                .withForce(true)
//                .exec();
        System.out.println("ok");

        //swarm  init
//        dockerClient.initializeSwarmCmd(new SwarmSpec())
//                .withListenAddr("127.0.0.1")
//                .withAdvertiseAddr("127.0.0.1")
//                .exec();

        //create service
//        List<PortConfig> ports=new ArrayList<>();
//        ports.add(new PortConfig().withTargetPort(80));
//
//        CreateServiceResponse createServiceResponse= dockerClient.createServiceCmd(new ServiceSpec()
//                .withName(service_name)
//                .withEndpointSpec(new EndpointSpec().withPorts(ports)) // 设置端口
//                .withMode(new ServiceModeConfig()
//                        .withReplicated(new ServiceReplicatedModeOptions()
//                                .withReplicas(2))) //设置副本数量
//                .withTaskTemplate(
//                        new TaskSpec()
//                                .withResources(new ResourceRequirements()
//                                        .withLimits(new ResourceSpecs()
//                                                .withNanoCPUs(1000000000)
//                                                .withMemoryBytes(2078494720)))
//                                .withContainerSpec(new ContainerSpec()
//                                        .withImage(image_name)
//                        )))
//                .exec();

        //========================
//        Map<String,String> map = new HashMap<String,String>();
//        map.put("type","nfs");
//        map.put("vers","4");
//        map.put("device","192.168.81.140:/home/ma/connection/ms");
//        map.put("opt","addr=192.168.81.140,soft,timeo=180,bg,tcp");
//        List<PortConfig> portConfigs=new ArrayList<>();
//        portConfigs.add(new PortConfig().withTargetPort(80));
//        Mount tmpMount = new Mount().withSource("pan").withTarget("/usr/share/nginx/html").withReadOnly(false).withVolumeOptions(new VolumeOptions().withDriverConfig(new Driver().withName("local").withOptions(map)));
//        List<Mount> mounts = new ArrayList<>();
//        mounts.add(tmpMount);
//        ServiceSpec spec = new ServiceSpec()
//                .withName("nginx1")
//                //设置数量
//                .withMode(new ServiceModeConfig().withReplicated(new ServiceReplicatedModeOptions().withReplicas(3)))
//                .withEndpointSpec(new EndpointSpec().withPorts(portConfigs))
//                .withTaskTemplate(new TaskSpec()
//                                .withContainerSpec(new ContainerSpec()
//                                        .withImage("nginx:latest")
//                                        .withMounts(mounts))
//                );
        //========================

//        List<SwarmNode> swarmNodes = dockerClient.listSwarmNodesCmd().exec();
//        System.out.println("=========11111111======swarmNodes============");
//        for(SwarmNode sn:swarmNodes) {
//            System.out.println(sn);
//            System.out.println("id==" + sn.getId());
//            System.out.println("cpu==" + sn.getDescription().getResources().getNanoCPUs());
//            System.out.println("mem==" + sn.getDescription().getResources().getMemoryBytes());
//            System.out.println("Hostname==" + sn.getDescription().getHostname());
//        }
//
//
//        System.out.println("==========222222222222===============");
//        System.out.println("inspectService===" + dockerClient.inspectServiceCmd(service_name).exec());
//        System.out.println("port===" + dockerClient.inspectServiceCmd(service_name).exec().getEndpoint().getPorts()[0].getPublishedPort());

//        dockerClient.inspectServiceCmd(service_name).exec();


//        System.out.println("===============node cmd=====================");
//        for (int i=0 ; i<swarmNodes.size() ; i++){
//            System.out.println(swarmNodes.get(i).getDescription());
//        }

//        List<Service> services = dockerClient.listServicesCmd()
//                .withNameFilter(Lists.newArrayList(service_name))
//                .exec();
//
//        ServiceSpec serviceSpec = dockerClient.inspectServiceCmd(service_name).exec().getSpec();
//
//        String service_id = dockerClient.inspectServiceCmd(service_name).exec().getId();
//        System.out.println("===============service_id=====================");
//        System.out.println(service_id);

//        List<Task> tasks = dockerClient.listTasksCmd().withNameFilter("test_nginx").withStateFilter(TaskState.RUNNING).exec();

//        List<Task> tasks = dockerClient.listTasksCmd().exec();
//        System.out.println("========33333333333333=========tasks=====================");
//        ArrayList<String> node_id = new ArrayList<>();
//
//        for (int i=0 ; i < tasks.size() ;i++){
//            System.out.println(tasks.get(i));
//            System.out.println("NodeId=" + tasks.get(i).getNodeId());
//            node_id.add(tasks.get(i).getNodeId());
//        }

//        System.out.println("========44444444444444=========python=====success=======");
        //根据结点id的到宿主机ip
//        String exe = "python";
//        String command = "D:\\test_docker\\get_ip.py";
//        String num1 = node_id.get(0);
//        String ip = "192.168.100.67";
//        String[] cmdArr = new String[] {exe, command, num1, ip};
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec(cmdArr);
//            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                System.out.println("line=============" + line);
//            }
//
//            process.waitFor();
//            br.close();
//            process.destroy();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        List<SwarmNode> swarmNodeList = dockerClient.listSwarmNodesCmd().withIdFilter(node_id).exec();
//        System.out.println("===============nodes=====================");
//        System.out.println("node_num:" + swarmNodeList.size());
//        for (int i=0 ; i<swarmNodeList.size() ; i++){
//            System.out.println(swarmNodeList.get(i));
//            System.out.println(swarmNodeList.get(i).getSpec());
//            System.out.println(swarmNodeList.get(i).getDescription().getHostname());
//            System.out.println(swarmNodeList.get(i).getStatus());
//        }


//        System.out.println("===============service=====================");
//        System.out.println(services.get(0));

//        Thread.sleep(5000);

//        dockerClient.removeServiceCmd(service_name).exec();
    }

    public static void main(String[] args) throws InterruptedException {

        String service_name = "test_nginx_2";
        String image_name = "nginx";

        testCreateService(service_name,image_name);

    }

}
