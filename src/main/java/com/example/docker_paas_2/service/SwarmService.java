package com.example.docker_paas_2.service;

import com.alibaba.fastjson.JSONObject;
import com.example.docker_paas_2.entry.MyContainer;
import com.example.docker_paas_2.entry.ServiceDetail;
import com.example.docker_paas_2.entry.Swarm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;


public interface SwarmService {

    //查看镜像
    public ArrayList<String> image_view(String user_name);

    //申请mysql服务
    public JSONObject service_create_mysql(String user_name, String image_name, String service_name,
                                 int copy_num, String mysql_pwd, int cpu, int ram);

    //申请java服务
    public JSONObject service_create_java(String user_name, String image_name, String service_name,
                                     int copy_num, MultipartFile java_file, String java_cmd, int cpu, int ram);

    //申请nginx服务
    public JSONObject service_create_nginx(String user_name, String image_name, String service_name,
                                     int copy_num, int cpu, int ram);

    //申请其他服务
    public JSONObject service_create_other(String user_name, String image_name, String service_name,
                                           int copy_num, int cpu, int ram);

    //用户查看自己的服务
    public ArrayList<Swarm> service_view(String user_name);

    //管理员查看所有服务
    public ArrayList<Swarm> service_view_all(String admin_name);

    //管理员根据用户名查看服务
    public ArrayList<Swarm> service_view_by_user_name(String admin_name, String user_name);

    //管理员根据服务名查看服务
    public Swarm view_by_service(String admin_name, String service_name);

    //管理员根据镜像名查看服务
    public ArrayList<Swarm> view_by_image(String admin_name, String image_name);

    //删除服务
    public String service_delete(String service_name, String user_name);

    //查看某服务的所有容器副本信息
    public ArrayList<MyContainer> copy_view(String service_name, String user_name);

    //查看某一项服务的具体信息
    public ServiceDetail service_detail(String service_name, String user_name);

    //用户上传文件到容器
    public JSONObject service_upfile(MultipartFile file, String service_name, String user_name);

    //用户请求查看download文件夹里的所有文件名
    public ArrayList<String> lookover_filename(String service_name, String user_name);

    //用户下载文件
    public ResponseEntity<byte[]> service_downfile(String filename, String service_name, String user_name) throws IOException;

    //请求修改服务副本数目
    public String modify_copy_num(String service_name, int copy_num, String user_name);

    //请求调整cpu，内存配置
    public String modify_resource(String service_name, int cpu, int ram, String user_name);

    //管理员上传镜像
    public String image_upload(String admin_name, String image_name);

    //管理员删除镜像
    public String image_delete(String admin_name, String image_name);

}
