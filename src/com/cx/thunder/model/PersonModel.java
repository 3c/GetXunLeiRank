package com.cx.thunder.model;
public class PersonModel {

    public String name;
    public String region;
    public int isme;
    public int isvip;
    public int rank;
    public int rank_display;
    public int innerno;
    public int exp;
    public int title_type;
    public int region_id;
    public int oltime;
    public float dlgiga;
    public int dlfile;
    public String level_img;
    public String vip_level="未知";

    @Override
    public String toString() {
        return "PersonModel [name=" + name + ", region=" + region + ", isme=" + isme + ", isvip=" + isvip + ", rank="
                + rank + ", rank_display=" + rank_display + ", innerno=" + innerno + ", exp=" + exp + ", title_type="
                + title_type + ", region_id=" + region_id + ", oltime=" + oltime + ", dlgiga=" + dlgiga + ", dlfile="
                + dlfile + ", level_img=" + level_img + ", vip_level=" + vip_level + "]";
    }



}
