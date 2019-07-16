package com.example.docker_paas_2.dao;


import com.example.docker_paas_2.entry.Swarm;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface SwarmDao {

    //根据服务名读出一条数据
    public Swarm getByServiceName(String service_name);

    //插入一条记录到数据库
    public int insertService(String user_name, String image_name,
                             String service_name, int copy_num, int cpu, int ram);

    //根据用户名从数据库读出自己所有的服务数据！！！可能不止一条
    public ArrayList<Swarm> getByUserName(String user_name);

    //管理员从数据库读出所有服务数据
    public ArrayList<Swarm> getAllService();

    //根据服务名删除一条记录
    public int deleteByServiceName(String service_name);

    //更新副本数
    public int updateServiceCopyNum(String service_name, int copy_num);

    //更新cpu，内存配置
    public int updateServiceResource(String service_name, int cpu, int ram);

    //根据镜像名从数据库读出服务数据
    public ArrayList<Swarm> getByImageName(String image_name);

}
