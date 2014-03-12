import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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
     * 经验值数据
     */
    static String[] expArr = null;
    /**
     * vip等级数组
     */
    static String[] vipArr = null;

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
        // write();
    }

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
                    String privicenUrl = "http://dynamic.i.xunlei.com/ajax?c=site&a=topList&type1=1&type2=0&ext=";
                    ArrayList<PersonModel> listCache = new ArrayList<PersonModel>();
                    int time = 0;
                    // 循环34个省
                    for (int i = 1; i < 35; i++) {

                        // 间隔1-10S ,再去请求，防止400错误
                        time = (int) (Math.random() * 9000) + 1000;
                        String requestUrl = privicenUrl + i + "&cachetime=" + System.currentTimeMillis();
                        System.out.println(i + " : " + requestUrl);
                        String content = sendGet(requestUrl);
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
                    String chenghaoUrl = "http://dynamic.i.xunlei.com/ajax?c=site&a=topList&type1=2&type2=0&ext=";
                    for (int i = 0; i < 5; i++) {
                        time = (int) (Math.random() * 9000) + 1000;
                        String requestUrl = chenghaoUrl + i + "&cachetime=" + System.currentTimeMillis();
                        System.out.println(i + " ： " + requestUrl);
                        String content = sendGet(requestUrl);
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
                    // 写入sd卡，以防以外.如果write方法有误，可以在main里直接调用write方法进行调试，不用每次都等待read完毕
                    writeFileToSD(today + Config.SUFFIX_TEXT, content);


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
            String[][] result = getData(file, 1);
            int rowLength = result.length;

            expArr = new String[rowLength];
            vipArr = new String[rowLength];
            for (int i = 0; i < rowLength; i++) {

                for (int j = 0; j < result[i].length; j++) {
                    if (j == 2) {
                        expArr[i] = result[i][j];
                    } else if (j == 5) {
                        vipArr[i] = result[i][j];
                    }
                    // System.out.print(result[i][j]+"\t\t");
                }
                // System.out.println();
            }
            // for (int i = 0; i < expArr.length; i++) {
            // System.out.println(expArr[i]);
            // }
        } else {

            System.out.println("no yesterday data!!!");
        }



        String content = getJsonFromAssets(Config.FILE_PATH + today + Config.SUFFIX_TEXT);
        Type type = new TypeToken<List<PersonModel>>() {}.getType();
        list = new Gson().fromJson(content, type);
        System.out.println("write data size " + list.size());
        writeExcel(today + Config.SUFFIX_EXCEL);

    }

    /**
     * 生成一个Excel文件
     * 
     * @param fileName 要生成的Excel文件名
     */
    public static void writeExcel(String fileName) {
        // 第一步，创建一个webbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet("迅雷用户排行");
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
        HSSFRow row = sheet.createRow((int) 0);
        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        HSSFCell cell = row.createCell((short) 0);
        cell.setCellValue("排名");
        cell.setCellStyle(style);
        cell = row.createCell((short) 1);
        cell.setCellValue("用户名");
        cell.setCellStyle(style);
        cell = row.createCell((short) 2);
        cell.setCellValue("经验");
        cell.setCellStyle(style);
        cell = row.createCell((short) 3);
        cell.setCellValue("省份");
        cell.setCellStyle(style);
        cell = row.createCell((short) 4);
        cell.setCellValue("是否是会员");
        cell.setCellStyle(style);
        cell = row.createCell((short) 5);
        cell.setCellValue("会员等级");
        cell.setCellStyle(style);
        // 第五步，写入实体数据 实际应用中这些数据从数据库得到，

        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow((int) i + 1);
            PersonModel model = list.get(i);
            // 第四步，创建单元格，并设置值
            row.createCell((short) 0).setCellValue(i + 1);
            row.createCell((short) 1).setCellValue(model.name);
            row.createCell((short) 2).setCellValue(model.exp);
            row.createCell((short) 3).setCellValue(model.region);
            row.createCell((short) 4).setCellValue(model.isvip == 1 ? "是" : "不是");
            String vipLevel = "未知";
            if (expArr != null && vipArr != null) {
                int expAdd = model.exp - Integer.parseInt(expArr[i]);
                if (expAdd > 150) {
                    vipLevel = "白金vip7";
                } else if (expAdd > 140) {
                    vipLevel = "白金vip6";
                } else if (expAdd > 130) {
                    vipLevel = "白金vip5";
                } else if (expAdd > 120) {
                    vipLevel = "白金vip4|普通vip6";
                } else if (expAdd > 110) {
                    vipLevel = "白金vip3|普通vip5";
                } else if (expAdd > 100) {
                    vipLevel = "白金vip2|普通vip4";
                } else if (expAdd > 90) {
                    vipLevel = "白金vip1|普通vip3";
                } else if (expAdd > 80) {
                    vipLevel = "普通vip2";
                } else if (expAdd > 70) {
                    vipLevel = "普通vip1";
                }

                if ("白金vip7".equals(vipArr[i])) {
                    vipLevel = "白金vip7";
                } else if ("白金vip6".equals(vipArr[i])) {
                    vipLevel = "白金vip6";
                } else if ("白金vip5".equals(vipArr[i])) {
                    vipLevel = "白金vip5";
                } else if ("白金vip4|普通vip6".equals(vipArr[i])) {
                    vipLevel = "白金vip4|普通vip6";
                } else if ("白金vip3|普通vip5".equals(vipArr[i])) {
                    vipLevel = "白金vip3|普通vip5";
                } else if ("白金vip2|普通vip4".equals(vipArr[i])) {
                    vipLevel = "白金vip2|普通vip4";
                } else if ("白金vip1|普通vip3".equals(vipArr[i])) {
                    vipLevel = "白金vip1|普通vip3";
                } else if ("普通vip2".equals(vipArr[i])) {
                    vipLevel = "普通vip2";
                } else if ("普通vip1".equals(vipArr[i])) {
                    vipLevel = "普通vip1";
                }
            }
            row.createCell((short) 5).setCellValue(vipLevel);
        }
        // 第六步，将文件存到指定位置
        try {
            FileOutputStream fout = new FileOutputStream(Config.FILE_PATH + fileName);
            wb.write(fout);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeFileToSD(String fileName, String content) {
        try {
            String pathName = Config.FILE_PATH;
            File path = new File(pathName);
            File file = new File(pathName + fileName);
            if (!path.exists()) {
                path.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
            String s = content;
            byte[] buf = s.getBytes();
            stream.write(buf);
            stream.close();

        } catch (Exception e) {
            System.out.println("error + " + e.toString());
        }
    }

    public static String getJsonFromAssets(String filePath) {
        InputStream is;

        Writer writer = new StringWriter();
        char[] buffer = new char[8 * 1024];
        try {
            is = new FileInputStream(new File(filePath));
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }

        return writer.toString();
    }

    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            // for (String key : map.keySet()) {
            // System.out.println(key + "--->" + map.get(key));
            // }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
     * 
     * @param file 读取数据的源Excel
     * 
     * @param ignoreRows 读取数据忽略的行数，比喻行头不需要读入 忽略的行数为1
     * 
     * @return 读出的Excel中数据的内容
     * 
     * @throws FileNotFoundException
     * 
     * @throws IOException
     */

    public static String[][] getData(File file, int ignoreRows)

    throws FileNotFoundException, IOException {

        List<String[]> result = new ArrayList<String[]>();

        int rowSize = 0;

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(

        file));

        // 打开HSSFWorkbook

        POIFSFileSystem fs = new POIFSFileSystem(in);

        HSSFWorkbook wb = new HSSFWorkbook(fs);

        HSSFCell cell = null;

        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {

            HSSFSheet st = wb.getSheetAt(sheetIndex);

            // 第一行为标题，不取

            for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {

                HSSFRow row = st.getRow(rowIndex);

                if (row == null) {

                    continue;

                }

                int tempRowSize = row.getLastCellNum() + 1;

                if (tempRowSize > rowSize) {

                    rowSize = tempRowSize;

                }

                String[] values = new String[rowSize];

                Arrays.fill(values, "");

                boolean hasValue = false;

                for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {

                    String value = "";

                    cell = row.getCell(columnIndex);

                    if (cell != null) {

                        // 注意：一定要设成这个，否则可能会出现乱码

                        // cell.setEncoding(HSSFCell.ENCODING_UTF_16);

                        switch (cell.getCellType()) {

                            case HSSFCell.CELL_TYPE_STRING:

                                value = cell.getStringCellValue();

                                break;

                            case HSSFCell.CELL_TYPE_NUMERIC:

                                if (HSSFDateUtil.isCellDateFormatted(cell)) {

                                    Date date = cell.getDateCellValue();

                                    if (date != null) {

                                        value = new SimpleDateFormat("yyyy-MM-dd")

                                        .format(date);

                                    } else {

                                        value = "";

                                    }

                                } else {

                                    value = new DecimalFormat("0").format(cell

                                    .getNumericCellValue());

                                }

                                break;

                            case HSSFCell.CELL_TYPE_FORMULA:

                                // 导入时如果为公式生成的数据则无值

                                if (!cell.getStringCellValue().equals("")) {

                                    value = cell.getStringCellValue();

                                } else {

                                    value = cell.getNumericCellValue() + "";

                                }

                                break;

                            case HSSFCell.CELL_TYPE_BLANK:

                                break;

                            case HSSFCell.CELL_TYPE_ERROR:

                                value = "";

                                break;

                            case HSSFCell.CELL_TYPE_BOOLEAN:

                                value = (cell.getBooleanCellValue() == true ? "Y"

                                : "N");

                                break;

                            default:

                                value = "";

                        }

                    }

                    if (columnIndex == 0 && value.trim().equals("")) {

                        break;

                    }

                    values[columnIndex] = rightTrim(value);

                    hasValue = true;

                }



                if (hasValue) {

                    result.add(values);

                }

            }

        }

        in.close();

        String[][] returnArray = new String[result.size()][rowSize];

        for (int i = 0; i < returnArray.length; i++) {

            returnArray[i] = (String[]) result.get(i);

        }

        return returnArray;

    }



    /**
     * 
     * 去掉字符串右边的空格
     * 
     * @param str 要处理的字符串
     * 
     * @return 处理后的字符串
     */

    public static String rightTrim(String str) {

        if (str == null) {

            return "";

        }

        int length = str.length();

        for (int i = length - 1; i >= 0; i--) {

            if (str.charAt(i) != 0x20) {

                break;

            }

            length--;

        }

        return str.substring(0, length);

    }

}
