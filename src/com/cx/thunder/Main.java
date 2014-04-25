package com.cx.thunder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cx.thunder.config.Config;
import com.cx.thunder.config.EnumVipLevel;
import com.cx.thunder.model.PersonListModel;
import com.cx.thunder.model.PersonModel;
import com.cx.thunder.util.ComparatorUser;
import com.cx.thunder.util.ExcelUtil;
import com.cx.thunder.util.FileUtil;
import com.cx.thunder.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 主方法
 * 
 * @author CX
 * 
 */
public class Main {

    /**
     * 以 会员号 为key的map,用于去重
     */
    static HashMap<Integer, PersonModel> hashMap = new HashMap<Integer, PersonModel>();

    /**
     * 存储数据的列表
     */
    static ArrayList<PersonModel> list = new ArrayList<PersonModel>();

    /**
     * 昨天的存储数据列表，用于比较
     */
    static HashMap<Integer, PersonModel> hashMapYesterday = null;

    /**
     * 今天，用于命名。例如2014_03_12
     */
    static String today;

    /**
     * 经验值基准，暂没用，因为可能不准
     */
    static int STANDER_EXP = 220000;

    /**
     * 排名基准，已用。 这个数据更准些，只计算前800的用户
     */
    static int STANDER_RANK = 800;



    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParseException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        setFilePath();
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");// 设置日期格式
        today = "thunder_" + df.format(new Date());
        read();
//         write();
    }

    

    

    /**
     * 设置文件保存路径，默认是 工程目录/doc
     */
    public static void setFilePath() {
        String pathBin = Main.class.getClass().getResource("/").getFile();
        String path = pathBin.substring(0, pathBin.lastIndexOf("/"));
        path = path.substring(0, path.lastIndexOf("/"));
        path = path + "/doc/";
        Config.FILE_PATH = path;
        System.out.println("file save path " + Config.FILE_PATH);

    }



    /**
     * 从网页抓取数据
     */
    public static void read() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    // 省份链接
                    String privicenUrl = Config.PROVINCE_URL;
                    ArrayList<PersonModel> listCache = new ArrayList<PersonModel>();
                    int time = 0;
                    // 循环34个省
                    for (int i = 1; i < 35; i++) {

                        // 间隔1-10S ,再去请求，防止400错误
                        time = (int) (Math.random() * 9000) + 5000;
                        String requestUrl = privicenUrl + i + "&cachetime=" + System.currentTimeMillis();
                        System.out.println(i + " : " + requestUrl);
                        String content = HttpUtil.sendGet(requestUrl);
                        PersonListModel listModel = new Gson().fromJson(content, PersonListModel.class);
                        System.out.println(listModel.data.get(0).region + " : " + listModel.data.size());
                        for (PersonModel model : listModel.data) {
                            // 这里是第一次加，不会有重复的
                            hashMap.put(model.innerno, model);
                        }

                        Thread.sleep(time);
                        // 没请求10次，强制休息10S
                        if (i % 10 == 0) {
                            Thread.sleep(10000);
                        }
                    }
                    // 请求完省份，休息10S
                    Thread.sleep(10000);

                    // 称号URL ，这个能弥补部分没有选择省份的用户
                    String chenghaoUrl = Config.CHENGHAO_URL;
                    for (int i = 0; i < 5; i++) {
                        time = (int) (Math.random() * 9000) + 1000;
                        String requestUrl = chenghaoUrl + i + "&cachetime=" + System.currentTimeMillis();
                        System.out.println(i + " ： " + requestUrl);
                        String content = HttpUtil.sendGet(requestUrl);
                        PersonListModel listModel = new Gson().fromJson(content, PersonListModel.class);
                        System.out.println(i + " size " + listModel.data.size());
                        for (PersonModel model : listModel.data) {
                            // 去重
                            if (!hashMap.containsKey(model.innerno)) {
                                hashMap.put(model.innerno, model);
                            }
                        }
                        Thread.sleep(time);
                    }

                    // 遍历hashmap，把数据都放到 listCache中
                    Iterator iter = hashMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        listCache.add((PersonModel) entry.getValue());
                    }
                    System.out.println("list cache size " + listCache.size());

                    // 按经验值，降序排序
                    Collections.sort(listCache, new ComparatorUser());

                    // 基于经验值取数据
                    // for (int i = 0; i < listCache.size(); i++) {
                    // if (listCache.get(i).exp > STANDER) {
                    // list.add(listCache.get(i));
                    // } else {
                    // break;
                    // }
                    // }

                    // 基于排名取数据
                    for (int i = 0; i < STANDER_RANK; i++) {
                        list.add(listCache.get(i));
                    }
                    System.out.println("read data size " + list.size());

                    String content = new Gson().toJson(list);
                    // 写入本地，以防以外.如果write方法有误，可以在main里直接调用write方法进行调试，不用每次都等待read完毕
                    FileUtil.writeFileToSD(today + Config.SUFFIX_TEXT, content);


                    write();


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 将排名写入excel中
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void write() throws FileNotFoundException, IOException {


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = "thunder_" + new SimpleDateFormat("yyyy_MM_dd").format(cal.getTime());
        File file = new File(Config.FILE_PATH + yesterday + Config.SUFFIX_EXCEL);
        // 寻找昨天的数据，存在的话就读取，用于计算两天相差经验值
        if (file.exists()) {
            hashMapYesterday = new HashMap<Integer, PersonModel>();
            String[][] result = ExcelUtil.readExcel(file, 1);
            int rowLength = result.length;
            for (int i = 0; i < rowLength; i++) {

                PersonModel model = new PersonModel();

                for (int j = 0; j < result[i].length; j++) {
                    if (j == 2) {
                        model.name = result[i][j];
                    } else if (j == 3) {
                        model.exp = Integer.parseInt(result[i][j]);
                    } else if (j == 5) {
                        model.isvip = "是".equals(result[i][j]) ? 1 : 0;
                    } else if (j == 6) {
                        model.vip_level = EnumVipLevel.valueOf(result[i][j]);
                    } else if (j == 0) {
                        model.innerno = Integer.parseInt(result[i][j]);
                    }
                    // System.out.print(result[i][j]+"\t\t");
                }
                // System.out.println();

                hashMapYesterday.put(model.innerno, model);
            }
            // for (int i = 0; i < expArr.length; i++) {
            // System.out.println(expArr[i]);
            // }
        } else {

            System.out.println("no yesterday data!!!");
        }



        String content = FileUtil.getJsonFromAssets(Config.FILE_PATH + today + Config.SUFFIX_TEXT);
        Type type = new TypeToken<List<PersonModel>>() {}.getType();
        list = new Gson().fromJson(content, type);
        System.out.println("write data size " + list.size());
        ExcelUtil.writeExcel(today + Config.SUFFIX_EXCEL, list, hashMapYesterday);

    }



}
