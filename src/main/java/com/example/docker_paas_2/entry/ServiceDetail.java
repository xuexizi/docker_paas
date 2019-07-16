package com.example.docker_paas_2.entry;

public class ServiceDetail {
    private String image_name;
    private int copy_num;
    private int cpu;
    private int ram;

    private String mount_dir;
    private String work_dir;

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

    public String getMount_dir() {
        return mount_dir;
    }

    public void setMount_dir(String mount_dir) {
        this.mount_dir = mount_dir;
    }

    public String getWork_dir() {
        return work_dir;
    }

    public void setWork_dir(String work_dir) {
        this.work_dir = work_dir;
    }

}
