package com.example.docker_paas_2.entry;

public class Swarm {
    private String user_name;
    private String image_name;
    private String service_name;
    private int copy_num;
    private int cpu;
    private int ram;

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public int getCopy_num() {
        return copy_num;
    }

    public void setCopy_num(int copy_num) {
        this.copy_num = copy_num;
    }

}
