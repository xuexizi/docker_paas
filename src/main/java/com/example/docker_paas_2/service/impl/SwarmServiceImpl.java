package com.example.docker_paas_2.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.example.docker_paas_2.dao.JournalDao;
import com.example.docker_paas_2.dao.SwarmDao;
import com.example.docker_paas_2.entry.MyContainer;
import com.example.docker_paas_2.entry.ServiceDetail;
import com.example.docker_paas_2.entry.Swarm;
import com.example.docker_paas_2.service.SwarmService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateServiceResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class SwarmServiceImpl implements SwarmService {
    @Resource
    SwarmDao swarmDao;
    @Resource
    JournalDao journalDao;

    String ip = "192.168.100.109";
    DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://" + ip + ":2375").build();

    //查看镜像
    public ArrayList<String> image_view(String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, user_name, "查看镜像");

        ArrayList<String> image = new ArrayList<>();
        List<Image> image_name = dockerClient.listImagesCmd().exec();
        for(Image i:image_name) {
            if(i.getRepoTags()[0]==null) {
                continue;
            }
            System.out.println(i.getRepoTags()[0]);
            image.add(i.getRepoTags()[0]);
        }
        return image;
    }

    //申请mysql服务
    public JSONObject service_create_mysql(String user_name, String image_name, String service_name,
                                           int copy_num, String mysql_pwd, int cpu, int ram) {

        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        JSONObject resObj = new JSONObject();

        //查看申请的资源是否在范围内
        if(check_rescource(copy_num, cpu, ram) == false) {
            journalDao.insertJournal(nowTime, user_name, "申请mysql服务失败-->资源不在范围内");
            resObj.put("msg", "resource_not_limit");
            return resObj;
        }

        //查看服务名是否已经存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm != null) {
            journalDao.insertJournal(nowTime, user_name, "申请mysql服务失败-->服务名已存在");
            resObj.put("msg", "service_name_exist");
            return resObj;
        }

        //========创建挂载文件夹=================
        String path1 = "/docker_paas_file/" + service_name + "/conf/";
        String path2 = "/docker_paas_file/" + service_name + "/logs/";
        String path3 = "/docker_paas_file/" + service_name + "/data/";
//        String path4 = "/docker_paas_file/" + service_name + "/upload_file/";
        String path5 = "/docker_paas_file/" + service_name + "/download_file/";
        ArrayList<String> array_mysql = new ArrayList<String>(Arrays.asList(path1, path2, path3, path5));
        for(String s:array_mysql) {
            File file = new File(s);
            file.mkdirs();
        }

        String target_path1 = "/etc/mysql/conf.d";
        String target_path2 = "/logs";
        String target_path3 = "/var/lib/mysql";
//        String target_path4 = "/upload_file";
        String target_path5 = "/download_file";
        ArrayList<String> target_list = new ArrayList<String>(Arrays.asList(target_path1, target_path2, target_path3, target_path5));

        int i = 0;
        List<Mount> mounts = new ArrayList<Mount>();
        for(String s:target_list) {
            Mount mount = new Mount().withTarget(s).withReadOnly(false).withVolumeOptions(new VolumeOptions().withDriverConfig(new Driver().withName("local").withOptions(getMapByDir(array_mysql.get(i)))));
            mounts.add(mount);
            i++;
        }
        //==============================

        //设置启动命令
        List<String> mysql_cmd = new ArrayList<String>();
        mysql_cmd.add("MYSQL_ROOT_PASSWORD=" + mysql_pwd);

        //设置端口映射
        List<PortConfig> ports = new ArrayList<>();
        ports.add(new PortConfig().withTargetPort(3306));

        CreateServiceResponse serviceResponse = dockerClient.createServiceCmd(new ServiceSpec()
                .withName(service_name)
                .withEndpointSpec(new EndpointSpec().withPorts(ports)) // 设置端口
                .withMode(new ServiceModeConfig()
                        .withReplicated(new ServiceReplicatedModeOptions()
                                .withReplicas(copy_num))) //设置副本数量
                .withTaskTemplate(
                        new TaskSpec()
                                .withResources(new ResourceRequirements()
                                        .withLimits(new ResourceSpecs()
                                                .withNanoCPUs(cpu*1000000000/2)  //设置cpu
                                                .withMemoryBytes(ram*1024*1024))) //设置ram
                                .withContainerSpec(new ContainerSpec()
                                        .withImage(image_name)
                                        .withMounts(mounts) //挂载
                                        .withEnv(mysql_cmd) //设置mysql密码
                                        .withTty(true)
                                )))
                .exec();

        //创建成功，写到数据库里
        journalDao.insertJournal(nowTime, user_name, "申请mysql服务成功");
        return insert_to_mysql(user_name, image_name, service_name, copy_num, cpu, ram);
    }

    //申请java服务
    public JSONObject service_create_java(String user_name, String image_name, String service_name,
                                          int copy_num, MultipartFile java_file, String java_cmd,
                                          int cpu, int ram) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        JSONObject resObj = new JSONObject();

        //查看申请的资源是否在范围内
        if(check_rescource(copy_num, cpu, ram) == false) {
            journalDao.insertJournal(nowTime, user_name, "申请java服务失败-->资源不在范围内");
            resObj.put("msg", "resource_not_limit");
            return resObj;
        }

        //查看服务名是否已经存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm != null) {
            journalDao.insertJournal(nowTime, user_name, "申请java服务失败-->服务名已存在");
            resObj.put("msg", "service_name_exist");
            return resObj;
        }

        //创建挂载文件夹=======================
        String path1 = "/docker_paas_file/" + service_name + "/myapp/";
//        String path2 = "/docker_paas_file/" + service_name + "/upload_file/";
        String path3 = "/docker_paas_file/" + service_name + "/download_file/";
        ArrayList<String> array_mysql = new ArrayList<String>(Arrays.asList(path1, path3));
        for(String s:array_mysql) {
            File file = new File(s);
            file.mkdirs();
        }

        String target_path1 = "/usr/src/myapp/";
//        String target_path2 = "/upload_file/";
        String target_path3 = "/download_file/";
        ArrayList<String> target_list = new ArrayList<String>(Arrays.asList(target_path1, target_path3));

        int i = 0;
        List<Mount> mounts = new ArrayList<Mount>();
        for(String s:target_list) {
            Mount mount = new Mount().withTarget(s).withReadOnly(false).withVolumeOptions(new VolumeOptions().withDriverConfig(new Driver().withName("local").withOptions(getMapByDir(array_mysql.get(i)))));
            mounts.add(mount);
            i++;
        }
        //======================

        //接收文件
        String ret_str = recv_file(java_file, path1);
        if(ret_str.equals("fail")) {
            journalDao.insertJournal(nowTime, user_name, "申请java服务失败-->上传文件失败");
            resObj.put("msg", "upload_file_fail");
            return resObj;
        }

        //设置java命令
        List<String> java_cmd_list = new ArrayList<>();
        String[] strings = java_cmd.split(" ");
        for(String s:strings) {
            java_cmd_list.add(s);
        }

        //设置端口
        List<PortConfig> ports=new ArrayList<>();
        ports.add(new PortConfig().withTargetPort(8080));

        CreateServiceResponse serviceResponse = dockerClient.createServiceCmd(new ServiceSpec()
                .withName(service_name)
                .withEndpointSpec(new EndpointSpec().withPorts(ports)) // 设置端口
                .withMode(new ServiceModeConfig()
                        .withReplicated(new ServiceReplicatedModeOptions()
                                .withReplicas(copy_num))) //设置副本数量
                .withTaskTemplate(
                        new TaskSpec()
                                .withResources(new ResourceRequirements()
                                        .withLimits(new ResourceSpecs()
                                                .withNanoCPUs(cpu*1000000000/2)  //设置cpu
                                                .withMemoryBytes(ram*1024*1024))) //设置ram
                                .withContainerSpec(new ContainerSpec()
                                        .withImage(image_name)
                                        .withDir("/usr/src/myapp/")  //设置工作路径
                                        .withMounts(mounts)  //挂载
                                        .withCommand(java_cmd_list)  //设置java运行命令
                                        .withTty(true)
                                )))
                .exec();

        //创建成功，写到数据库里
        journalDao.insertJournal(nowTime, user_name, "申请java服务成功");
        return insert_to_mysql(user_name, image_name, service_name, copy_num, cpu, ram);
    }

    //申请nginx服务
    public JSONObject service_create_nginx(String user_name, String image_name, String service_name,
                                           int copy_num, int cpu, int ram) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        JSONObject resObj = new JSONObject();

        //查看申请的资源是否在范围内
        if(check_rescource(copy_num, cpu, ram) == false) {
            journalDao.insertJournal(nowTime, user_name, "申请nginx服务失败-->资源不在范围内");
            resObj.put("msg", "resource_not_limit");
            return resObj;
        }

        //查看服务名是否已经存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm != null) {
            journalDao.insertJournal(nowTime, user_name, "申请nginx服务失败-->服务名已存在");
            resObj.put("msg", "service_name_exist");
            return resObj;
        }

        //=======================
        //创建挂载文件夹
        String path1 = "/docker_paas_file/" + service_name + "/www/";
        String path2 = "/docker_paas_file/" + service_name + "/conf/";
        String path3 = "/docker_paas_file/" + service_name + "/logs/";
//        String path4 = "/docker_paas_file/" + service_name + "/upload_file/";
        String path5 = "/docker_paas_file/" + service_name + "/download_file/";
        ArrayList<String> array_nginx = new ArrayList<String>(Arrays.asList(path1, path2, path3, path5));
        for(String s:array_nginx) {
            File file = new File(s);
            file.mkdirs();
        }

        String target_path1 = "/usr/share/nginx/html";
        String target_path2 = "/etc/nginx/";
        String target_path3 = "/var/log/nginx";
//        String target_path4 = "/upload_file";
        String target_path5 = "/download_file";
        ArrayList<String> target_list = new ArrayList<String>(Arrays.asList(target_path1, target_path2, target_path3, target_path5));

        int i = 0;
        List<Mount> mounts = new ArrayList<Mount>();
        for(String s:target_list) {
            Mount mount = new Mount().withTarget(s).withReadOnly(false).withVolumeOptions(new VolumeOptions().withDriverConfig(new Driver().withName("local").withOptions(getMapByDir(array_nginx.get(i)))));
            mounts.add(mount);
            i++;
        }
        //======================

        //设置端口
        List<PortConfig> ports = new ArrayList<>();
        ports.add(new PortConfig().withTargetPort(80));

        CreateServiceResponse serviceResponse = dockerClient.createServiceCmd(new ServiceSpec()
                .withName(service_name)
                .withEndpointSpec(new EndpointSpec().withPorts(ports)) // 设置端口
                .withMode(new ServiceModeConfig()
                        .withReplicated(new ServiceReplicatedModeOptions()
                                .withReplicas(copy_num))) //设置副本数量
                .withTaskTemplate(
                        new TaskSpec()
                                .withResources(new ResourceRequirements()
                                        .withLimits(new ResourceSpecs()
                                                .withNanoCPUs(cpu*1000000000/2)  //设置cpu
                                                .withMemoryBytes(ram*1024*1024))) //设置ram
                                .withContainerSpec(new ContainerSpec()
                                        .withImage(image_name)
                                        .withMounts(mounts) //挂载
                                        .withTty(true)
                                )))
                .exec();

        //创建成功，写到数据库里
        journalDao.insertJournal(nowTime, user_name, "申请nginx服务成功");
        return insert_to_mysql(user_name, image_name, service_name, copy_num, cpu, ram);
    }

    //申请其他服务
    public JSONObject service_create_other(String user_name, String image_name, String service_name,
                                           int copy_num, int cpu, int ram) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        JSONObject resObj = new JSONObject();

        //查看申请的资源是否在范围内
        if(check_rescource(copy_num, cpu, ram) == false) {
            journalDao.insertJournal(nowTime, user_name, "申请" + service_name + "服务失败-->资源不在范围内");
            resObj.put("msg", "resource_not_limit");
            return resObj;
        }

        //查看服务名是否已经存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm != null) {
            journalDao.insertJournal(nowTime, user_name, "申请" + service_name + "服务失败-->服务名已存在");
            resObj.put("msg", "service_name_exist");
            return resObj;
        }

        //=======================
        //创建挂载文件夹
        String path = "/docker_paas_file/" + service_name + "/download_file/";
        File file = new File(path);
        file.mkdirs();

        String target_path = "/download_file";
        List<Mount> mounts = new ArrayList<Mount>();
        Mount mount = new Mount().withTarget(target_path).withReadOnly(false).withVolumeOptions(new VolumeOptions().withDriverConfig(new Driver().withName("local").withOptions(getMapByDir(path))));
        mounts.add(mount);
        //======================

        //设置端口
        List<PortConfig> ports = new ArrayList<>();
        ports.add(new PortConfig().withTargetPort(80));

        CreateServiceResponse serviceResponse = dockerClient.createServiceCmd(new ServiceSpec()
                .withName(service_name)
                .withEndpointSpec(new EndpointSpec().withPorts(ports)) // 设置端口
                .withMode(new ServiceModeConfig()
                        .withReplicated(new ServiceReplicatedModeOptions()
                                .withReplicas(copy_num))) //设置副本数量
                .withTaskTemplate(
                        new TaskSpec()
                                .withResources(new ResourceRequirements()
                                        .withLimits(new ResourceSpecs()
                                                .withNanoCPUs(cpu*1000000000/2)  //设置cpu
                                                .withMemoryBytes(ram*1024*1024))) //设置ram
                                .withContainerSpec(new ContainerSpec()
                                        .withImage(image_name)
                                        .withMounts(mounts) //挂载
                                        .withTty(true)
                                )))
                .exec();

        //创建成功，写到数据库里
        journalDao.insertJournal(nowTime, user_name, "申请" + service_name + "服务成功");
        return insert_to_mysql(user_name, image_name, service_name, copy_num, cpu, ram);

    }

    //用户查看自己的服务
    public ArrayList<Swarm> service_view(String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, user_name, "用户查看自己的服务");

        return swarmDao.getByUserName(user_name);
    }

    //管理员查看所有服务
    public ArrayList<Swarm> service_view_all(String admin_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "管理员查看所有服务");

        return swarmDao.getAllService();
    }

    //管理员根据用户名查看服务
    public ArrayList<Swarm> service_view_by_user_name(String admin_name, String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "管理员根据用户名查看用户服务，用户名：" + user_name);

        return swarmDao.getByUserName(user_name);
    }

    //管理员根据服务名查看服务
    public Swarm view_by_service(String admin_name, String service_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "管理员根据服务名查看用户服务，服务名：" + service_name);

        return swarmDao.getByServiceName(service_name);
    }

    //管理员根据镜像名查看服务
    public ArrayList<Swarm> view_by_image(String admin_name, String image_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "管理员根据镜像名查看用户服务，镜像名：" + image_name);

        return swarmDao.getByImageName(image_name);
    }


    //删除服务。
    public String service_delete(String service_name, String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);


        //首先去数据库查看是否有该服务
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "删除服务失败-->服务不存在");
            return "service_not_exist";
        }

        //删除服务
        try {
            dockerClient.removeServiceCmd(service_name).exec();
        } catch (Exception e) {
            journalDao.insertJournal(nowTime, user_name, "删除服务失败-->docker删除失败");
            return "fail";
        }

        //删除挂载路径
        String path = "/docker_paas_file/" + service_name; //宿主机路径
        delFolder(path);

        //把数据库里的数据删除
        int ret = swarmDao.deleteByServiceName(service_name);
        if(ret > 0) {
            journalDao.insertJournal(nowTime, user_name, "删除服务成功,服务名：" + service_name);
            return "success";
        } else {
            journalDao.insertJournal(nowTime, user_name, "删除服务失败-->数据库删除失败");
            return "fail";
        }
    }

    //查看某服务的所有容器副本信息
    public ArrayList<MyContainer> copy_view(String service_name, String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        ArrayList<MyContainer> myContainers = new ArrayList<MyContainer>();

        //查看服务名是否存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "查看某服务的所有容器副本信息失败-->服务名不存在");
            return myContainers;
        }

        List<Task> tasks = dockerClient.listTasksCmd().withNameFilter(service_name).withStateFilter(TaskState.RUNNING).exec();

        for (Task task:tasks) {
            MyContainer myContainer = new MyContainer();
            //获取容器id
            myContainer.setContainer_id(task.getStatus().getContainerStatus().getContainerID());
            //获取host_ip
            myContainer.setHost_ip(get_ip(task.getNodeId()));
            //获取宿主机端口
            int publishedPort = dockerClient.inspectServiceCmd(service_name).exec().getEndpoint().getPorts()[0].getPublishedPort();
            myContainer.setHost_port(publishedPort);
            //获取宿主机端口
            int targetPort = dockerClient.inspectServiceCmd(service_name).exec().getEndpoint().getPorts()[0].getTargetPort();
            myContainer.setContainer_port(targetPort);

            myContainers.add(myContainer);
        }

        journalDao.insertJournal(nowTime, user_name, "查看某服务的所有容器副本信息,服务名：" + service_name);
        return myContainers;
    }

    //查看某一项服务的具体信息
    public ServiceDetail service_detail(String service_name, String user_name) {
        ServiceDetail serviceDetail = new ServiceDetail();

        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        //查看服务名是否存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "查看某一项服务的具体信息失败-->服务名不存在");
            return serviceDetail;
        }

        //赋值给serviceDetail
        serviceDetail.setImage_name(swarm.getImage_name());
        serviceDetail.setCopy_num(swarm.getCopy_num());
        serviceDetail.setCpu(swarm.getCpu());
        serviceDetail.setRam(swarm.getRam());

        //设置挂载路径，工作路径
        String mount_dir = "/docker_paas_file/" + service_name + "/";
        String work_dir = "";
        if(swarm.getImage_name().contains("java")) {
            work_dir = "/usr/src/myapp/";
        } else {
            work_dir = "/";
        }
        serviceDetail.setMount_dir(mount_dir);
        serviceDetail.setWork_dir(work_dir);

        journalDao.insertJournal(nowTime, user_name, "查看某一项服务的具体信息,服务名：" + service_name);
        return serviceDetail;
    }

    //用户上传文件到容器
    public JSONObject service_upfile(MultipartFile file, String service_name, String user_name) {
        JSONObject resObj = new JSONObject();

        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        //第一步，判断服务是否存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "上传文件到容器失败-->服务名不存在");
            resObj.put("msg", "service_not_exist");
            return resObj;
        }

        //第二步，接收文件存储下来,路径："/home/docker_paas_file/服务名/;
//        String down_path = "/docker_paas_file/" + service_name + "/upload_file/"; //宿主机的挂载目录
        String down_path = "/docker_paas_file/" + service_name + "/download_file/"; //宿主机的挂载目录
        String ret_str = recv_file(file, down_path);
        if(ret_str.equals("fail")) {
            journalDao.insertJournal(nowTime, user_name, "上传文件到容器失败-->上传失败");
            resObj.put("msg", "fail");
            return resObj;
        }

        journalDao.insertJournal(nowTime, user_name, "上传文件到容器, 服务名：" + service_name);
        resObj.put("msg", "success");
        return resObj;
    }

    //用户请求查看download文件夹里的所有文件名
    public ArrayList<String> lookover_filename(String service_name, String user_name) {
        ArrayList<String> fileNameList = new ArrayList<>();

        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        //查看服务是否存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "请求查看download文件夹失败-->服务名不存在");
            return fileNameList;
        }

        String path = "/docker_paas_file/" + service_name + "/download_file/";
        File file = new File(path);
        File[] tempList = file.listFiles();

        //如果是文件夹，就跳过，不显示
        for (File f:tempList) {
            if (f.isFile()) {
                fileNameList.add(f.getName());
            }
        }

        journalDao.insertJournal(nowTime, user_name, "请求查看download文件夹, 服务名：" + service_name);
        return fileNameList;
    }

    //用户下载文件
    public ResponseEntity<byte[]> service_downfile(String filename, String service_name, String user_name) throws IOException{
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        String path = "/docker_paas_file/" + service_name + "/download_file/";
        File file = new File(path + File.separator + filename);

        //如果文件不存在
        if(!file.exists()) {
            journalDao.insertJournal(nowTime, user_name, "请求下载文件失败-->文件不存在");
            return null;
        }

        journalDao.insertJournal(nowTime, user_name, "请求下载文件, 服务名：" + service_name + ",文件名：" + filename);

        HttpHeaders headers=new HttpHeaders();
        String downloadFileName = null;
        try {
            //防止文件名乱码
            downloadFileName = new String(filename.getBytes("UTF-8"),"iso-8859-1");
            //让电脑显示apach下载方式
            headers.setContentDispositionFormData("attachment", downloadFileName);
            //设置传输利用二进制传输
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //返回一个数据字节流，就是一个文件
        return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),headers, HttpStatus.CREATED);
    }

    //请求修改服务副本数目
    public String modify_copy_num(String service_name, int copy_num, String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        //查看服务名是否存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "请求修改服务副本数目失败-->服务名不存在");
            return "service_not_exist";
        }

        if(copy_num > 10 || copy_num < 1) {
            journalDao.insertJournal(nowTime, user_name, "请求修改服务副本数目失败-->资源不在范围内");
            return "copy_num_not_limit";
        }

        try {
            List<com.github.dockerjava.api.model.Service> services = dockerClient.listServicesCmd().withNameFilter(Collections.singletonList(service_name)).exec();
            if (services.size() == 1) {
                com.github.dockerjava.api.model.Service service = services.get(0);
                dockerClient.updateServiceCmd(service_name, service.getSpec()
                        .withMode(new ServiceModeConfig()
                                .withReplicated(new ServiceReplicatedModeOptions()
                                        .withReplicas(copy_num))))
                        .withVersion(service.getVersion().getIndex()).exec();
            }
            journalDao.insertJournal(nowTime, user_name, "请求修改服务副本数目, 服务名：" + service_name + ",副本数：" + copy_num);
        } catch (Exception e) {
            e.printStackTrace();
            journalDao.insertJournal(nowTime, user_name, "请求修改服务副本数目失败-->docker修改错误");
            return "fail";
        }
        //数据更新写入数据库
        swarmDao.updateServiceCopyNum(service_name, copy_num);
        return "success";
    }

    //请求调整cpu，内存配置
    public String modify_resource(String service_name, int cpu, int ram, String user_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

        //查看服务名是否存在
        Swarm swarm = swarmDao.getByServiceName(service_name);
        if(swarm == null) {
            journalDao.insertJournal(nowTime, user_name, "请求调整cpu，内存配置失败-->服务名不存在");
            return "service_not_exist";
        }

        if(cpu > 8 || cpu < 1 || ram < 4 || ram > 1024*2) {
            journalDao.insertJournal(nowTime, user_name, "请求调整cpu，内存配置失败-->资源不在范围内");
            return "resource_not_limit";
        }

//        try {
//            List<com.github.dockerjava.api.model.Service> services = dockerClient.listServicesCmd().withNameFilter(Collections.singletonList(service_name)).exec();
//            if (services.size() == 1) {
//                com.github.dockerjava.api.model.Service service = services.get(0);
//                dockerClient.updateServiceCmd(service_name, service.getSpec()
//                        .withTaskTemplate(new TaskSpec()
//                                .withResources(new ResourceRequirements()
//                                        .withLimits(new ResourceSpecs()
//                                                .withNanoCPUs(cpu*1000000000/2)
//                                                .withMemoryBytes(ram*1024*1024)))))
//                        .withVersion(service.getVersion().getIndex()).exec();
//            }
//            journalDao.insertJournal(nowTime, user_name, "请求调整cpu，内存配置, 服务名：" + service_name + ",cpu：" + cpu + ",ram：" + ram);
//        } catch (Exception e) {
//            e.printStackTrace();
//            journalDao.insertJournal(nowTime, user_name, "请求调整cpu，内存配置失败-->docker修改错误");
//            return "fail";
//        }

        //数据更新写入数据库
        journalDao.insertJournal(nowTime, user_name, "请求调整cpu，内存配置, 服务名：" + service_name + ",cpu：" + cpu + ",ram：" + ram);
        swarmDao.updateServiceResource(service_name, cpu, ram);
        return "success";
    }

    //管理员上传镜像
    public String image_upload(String admin_name, String image_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "管理员上传镜像, 镜像名：" + image_name);

        try {
            dockerClient.pullImageCmd(image_name)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
            return "success";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "fail";
        }

    }

    //管理员删除镜像
    public String image_delete(String admin_name, String image_name) {
        //记录日志
        Date date = new Date();//获得系统时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        journalDao.insertJournal(nowTime, admin_name, "管理员删除镜像, 镜像名：" + image_name);

        //查看该镜像是否有服务在运行
        List<Swarm> swarms = swarmDao.getByImageName(image_name);
        if(!swarms.isEmpty()) {
            return "image_in_use";
        }

        try {
            dockerClient.removeImageCmd(image_name).exec();
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }

    }






    //申请服务写入数据库
    public JSONObject insert_to_mysql(String user_name, String image_name, String service_name,
                                      int copy_num, int cpu, int ram) {
        JSONObject resObj = new JSONObject();
        int ret = swarmDao.insertService(user_name, image_name, service_name, copy_num, cpu, ram);
        if(ret > 0) {
            resObj.put("msg", "success");
            return resObj;
        } else {
            resObj.put("msg", "fail");
            return resObj;
        }
    }

    //给一个路径，返回map
    public Map<String,String> getMapByDir(String path) {
        Map<String,String> map = new HashMap<String,String>();
        map.put("type","nfs");
        map.put("device","192.168.100.109:" + path);
        map.put("o","addr=192.168.100.109,vers=4,soft,timeo=180,bg,tcp,rw");
//        map.put("device",ip+":" + path);
//        map.put("o","addr="+ip+",vers=4,soft,timeo=180,bg,tcp,rw");
        return map;
    }

    //接收文件
    public String recv_file(MultipartFile file, String down_path) {
        String fileName = file.getOriginalFilename();

        File down_file = new File(down_path + fileName);
        if(!down_file.getParentFile().exists()){
            down_file.getParentFile().mkdirs();
        }

        try{
            file.transferTo(down_file);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return "fail";
        }catch (IOException e){
            e.printStackTrace();
            return "fail";
        }
        return "success";
    }

    //通过结点id获取host的ip
    public String get_ip(String node_id) {
        String host_ip = "";

        String exe = "python";
        String command = "/docker_paas/get_ip.py";
        String[] cmdArr = new String[] {exe, command, node_id, ip};
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdArr);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                host_ip += line;
            }
            process.waitFor();
            br.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "fail";
        }
        System.out.println("host_ip=" + host_ip);
        return host_ip;
    }

    //删除文件夹
    public  void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            File myFilePath = new File(folderPath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下所有文件
    public  boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            //路径最后的斜杠修改，一般是linux和windows的斜杠与反斜杠
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }

            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }else { //删除文件（包括链接之类的）
                temp.delete();
            }
        }
        return flag;
    }

    //查看申请的资源是否在范围内
    public boolean check_rescource(int copy_num, int cpu, int ram) {
        boolean flag = true;
        if(copy_num > 10 || copy_num < 1 || cpu > 8 || cpu < 1 || ram < 4 || ram > 1024*2) {
            flag = false;
        }
        return flag;
    }

}
