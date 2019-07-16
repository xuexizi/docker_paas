package com.example.docker_paas_2.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.docker_paas_2.entry.MyContainer;
import com.example.docker_paas_2.entry.ServiceDetail;
import com.example.docker_paas_2.entry.Swarm;
import com.example.docker_paas_2.service.SwarmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
@CrossOrigin
@ResponseBody
public class SwarmController {

    @Resource
    SwarmService swarmService;

    private static final Logger LOG = LoggerFactory.getLogger(SwarmController.class);

    //用户查看镜像
    @RequestMapping(value = "/request_image_view")
    public ArrayList<String> image_view(@RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("请求查看镜像=========");
        System.out.println("user_name:" + user_name);
        LOG.info("===用户查看镜像=== ");

        //调用service
        return swarmService.image_view(user_name);
    }

    //用户申请服务
    @RequestMapping(value = "/request_service_create")
    public JSONObject service_create(@RequestParam(value = "user_name") String user_name,
                                   @RequestParam(value = "image_name") String image_name,
                                   @RequestParam(value = "service_name") String service_name,
                                 @RequestParam(value = "copy_num") int copy_num,
                                 @RequestParam(value = "mysql_pwd", required = false) String mysql_pwd,
                                 @RequestParam(value = "java_file", required = false) MultipartFile java_file,
                                 @RequestParam(value = "java_cmd", required = false) String java_cmd,
                                 @RequestParam(value = "cpu") int cpu,
                                 @RequestParam(value = "ram") int ram) throws Exception{

        System.out.println("申请服务=========");
        System.out.println("user_name:" + user_name);
        System.out.println("image_name:" + image_name);
        System.out.println("service_name:" + service_name);
        System.out.println("copy_num:" + copy_num);
        System.out.println("cpu:" + cpu);
        System.out.println("ram:" + ram);

        JSONObject resObj = new JSONObject();
        //调用service
        if(image_name.contains("mysql")) {
            System.out.println("mysql_pwd:" + mysql_pwd);
            LOG.info("===申请服务=== " + "user_name:" + user_name + ",image_name:" + image_name + ",service_name:" + service_name + ",copy_num:" + copy_num + ",cpu:" + cpu + ",ram:" + ram + ",mysql_pwd:" + mysql_pwd);
            return swarmService.service_create_mysql(user_name, image_name, service_name, copy_num, mysql_pwd, cpu, ram);
        } else if(image_name.contains("java")) {
            if(java_file != null) {
                System.out.println("java_file:" + java_file.getOriginalFilename());
                LOG.info("===申请服务=== " + "user_name:" + user_name + ",image_name:" + image_name + ",service_name:" + service_name + ",copy_num:" + copy_num + ",cpu:" + cpu + ",ram:" + ram + ",java_file:" + java_file.getOriginalFilename());
                System.out.println("java_cmd:" + java_cmd);
                return swarmService.service_create_java(user_name, image_name, service_name, copy_num, java_file, java_cmd, cpu, ram);
            } else {
                resObj.put("msg", "file_is_null");
                return resObj;
            }
        } else if(image_name.contains("nginx")) {
            LOG.info("===申请服务=== " + "user_name:" + user_name + ",image_name:" + image_name + ",service_name:" + service_name + ",copy_num:" + copy_num + ",cpu:" + cpu + ",ram:" + ram);
            return swarmService.service_create_nginx(user_name, image_name, service_name, copy_num, cpu, ram);
        } else {
            LOG.info("===申请服务=== " + "user_name:" + user_name + ",image_name:" + image_name + ",service_name:" + service_name + ",copy_num:" + copy_num + ",cpu:" + cpu + ",ram:" + ram);
            return swarmService.service_create_other(user_name, image_name, service_name, copy_num, cpu, ram);
        }
    }

    //用户查看自己的服务
    @RequestMapping(value = "/request_service_view")
    public ArrayList<Swarm> service_view(@RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("查看自己的服务=========");
        System.out.println("user_name:" + user_name);
        LOG.info("===查看自己的服务=== " + "user_name:" + user_name);

        //调用service
        return swarmService.service_view(user_name);
    }

    //管理员查看所有服务
    @RequestMapping(value = "/request_service_view_all")
    public ArrayList<Swarm> service_view_all(@RequestParam(value = "admin_name") String admin_name) throws Exception{

        System.out.println("管理员查看所有服务=========");
        System.out.println("admin_name:" + admin_name);
        LOG.info("===管理员查看所有服务=== ");

        //调用service
        return swarmService.service_view_all(admin_name);
    }

    //管理员根据用户名查看服务
    @RequestMapping(value = "/request_view_by_user_name")
    public ArrayList<Swarm> service_view_user_name(@RequestParam(value = "admin_name") String admin_name,
                                                   @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("管理员根据用户名查看服务=========");
        System.out.println("admin_name:" + admin_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===管理员根据用户名查看服务=== ");

        //调用service
        return swarmService.service_view_by_user_name(admin_name, user_name);
    }

    //管理员根据服务名查看服务
    @RequestMapping(value = "/request_view_by_service_name")
    public Swarm view_by_service(@RequestParam(value = "admin_name") String admin_name,
                                 @RequestParam(value = "service_name") String service_name) throws Exception{

        System.out.println("管理员根据服务名查看服务=========");
        System.out.println("admin_name:" + admin_name);
        System.out.println("service_name:" + service_name);
        LOG.info("===管理员根据服务名查看服务=== ");

        //调用service
        return swarmService.view_by_service(admin_name, service_name);
    }

    //管理员根据镜像名查看服务
    @RequestMapping(value = "/request_view_by_image_name")
    public ArrayList<Swarm> view_by_image(@RequestParam(value = "admin_name") String admin_name,
                               @RequestParam(value = "image_name") String image_name) throws Exception{

        System.out.println("管理员根据镜像名查看服务=========");
        System.out.println("admin_name:" + admin_name);
        System.out.println("image_name:" + image_name);
        LOG.info("===管理员根据镜像名查看服务=== ");

        //调用service
        return swarmService.view_by_image(admin_name, image_name);
    }

    //删除服务
    @RequestMapping(value = "/request_service_delete")
    public String service_delete(@RequestParam(value = "service_name") String service_name,
                                 @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("删除服务=========");
        System.out.println("service_name:" + service_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===删除服务=== " + "service_name:" + service_name);

        //调用service
        return swarmService.service_delete(service_name, user_name);
    }

    //查看某一项服务的所有容器副本
    @RequestMapping(value = "/request_copy_view")
    public ArrayList<MyContainer> copy_view(@RequestParam(value = "service_name") String service_name,
                                            @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("查看服务的副本=========");
        System.out.println("service_name:" + service_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===查看服务的副本=== " + "service_name:" + service_name);

        //调用service
        return swarmService.copy_view(service_name, user_name);
    }

    //查看某一项服务的具体信息
    @RequestMapping(value = "/request_service_detail")
    public ServiceDetail service_detail(@RequestParam(value = "service_name") String service_name,
                                        @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("查看服务的具体信息=========");
        System.out.println("service_name:" + service_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===查看服务的具体信息=== " + "service_name:" + service_name);

        //调用service
        return swarmService.service_detail(service_name, user_name);
    }

    //上传文件到服务
    @RequestMapping(value = "/request_service_upfile")
    public JSONObject service_upfile(HttpServletRequest request,
                                     @RequestParam(value = "file") MultipartFile file,
                                     @RequestParam(value = "service_name") String service_name,
                                     @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("上传文件=========");
        System.out.println("file:" + file.getOriginalFilename());
        System.out.println("service_name:" + service_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===上传文件=== " + "service_name:" + service_name + ",file:" + file.getOriginalFilename());

        //调用service
        return swarmService.service_upfile(file, service_name, user_name);
    }

    //用户请求查看download文件夹里的所有文件名
    @RequestMapping(value = "/request_lookover_filename")
    public ArrayList<String> lookover_filename(@RequestParam(value = "service_name") String service_name,
                                               @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("查看download文件夹里的所有文件名=========");
        System.out.println("service_name:" + service_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===查看download文件夹里的所有文件名=== " + "service_name:" + service_name);

        //调用service
        return swarmService.lookover_filename(service_name, user_name);
    }

    //下载文件
    @RequestMapping(value = "/request_service_downfile")
    public ResponseEntity<byte[]> service_downfile(HttpServletRequest request,
                                                   @RequestParam(value = "filename") String filename,
                                                   @RequestParam(value = "service_name") String service_name,
                                                   @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("下载文件=========");
        System.out.println("filename:" + filename);
        System.out.println("service_name:" + service_name);
        System.out.println("user_name:" + user_name);
        LOG.info("===下载文件=== " + "service_name:" + service_name + ",filename:" + filename);

        //调用service
        return swarmService.service_downfile(filename, service_name, user_name);
    }

    //请求修改服务副本数目
    @RequestMapping(value = "/request_modify_copy_num")
    public String modify_copy_num(@RequestParam(value = "service_name") String service_name,
                                  @RequestParam(value = "copy_num") int copy_num,
                                  @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("增删容器副本数量=========");
        System.out.println("service_name:" + service_name);
        System.out.println("copy_num:" + copy_num);
        System.out.println("user_name:" + user_name);
        LOG.info("===下载文件=== " + "service_name:" + service_name + ",copy_num:" + copy_num);

        //调用service
        return swarmService.modify_copy_num(service_name, copy_num, user_name);
    }

    //请求调整cpu，内存配置
    @RequestMapping(value = "/request_modify_resource")
    public String modify_resource(@RequestParam(value = "service_name") String service_name,
                                  @RequestParam(value = "cpu") int copy_num,
                                  @RequestParam(value = "ram") int ram,
                                  @RequestParam(value = "user_name") String user_name) throws Exception{

        System.out.println("调整cpu，内存配置=========");
        System.out.println("service_name:" + service_name);
        System.out.println("copy_num:" + copy_num);
        System.out.println("ram:" + ram);
        System.out.println("user_name:" + user_name);
        LOG.info("===调整cpu，内存配置=== " + "service_name:" + service_name + ",copy_num:" + copy_num + ",ram:" + ram);

        //调用service
        return swarmService.modify_resource(service_name, copy_num, ram, user_name);
    }

    //管理员上传镜像
    @RequestMapping(value = "/request_image_upload")
    public String image_upload(@RequestParam(value = "admin_name") String admin_name,
                               @RequestParam(value = "image_name") String image_name) throws Exception{

        System.out.println("管理员上传镜像=========");
        System.out.println("admin_name:" + admin_name);
        System.out.println("image_name:" + image_name);
        LOG.info("===管理员上传镜像=== ");

        //调用service
        return swarmService.image_upload(admin_name, image_name);
    }

    //管理员删除镜像
    @RequestMapping(value = "/request_image_delete")
    public String image_delete(@RequestParam(value = "admin_name") String admin_name,
                               @RequestParam(value = "image_name") String image_name) throws Exception{

        System.out.println("管理员删除镜像=========");
        System.out.println("admin_name:" + admin_name);
        System.out.println("image_name:" + image_name);
        LOG.info("===管理员删除镜像=== ");

        //调用service
        return swarmService.image_delete(admin_name, image_name);
    }

}
