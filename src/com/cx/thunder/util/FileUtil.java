/**
 * Filename : FileUtil.java Author : CX Date : 2014-3-13
 * 
 * Copyright(c) 2011-2013 Mobitide Android Team. All Rights Reserved.
 */
package com.cx.thunder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.cx.thunder.config.Config;

/**
 * @author CX
 * 
 */
public class FileUtil {

    /**
     * 将文件写入本地路径
     * @param fileName
     * @param content
     */
    public static void writeFileToSD(String fileName, String content) {
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

    /**
     * 从本地路径读取json文件
     * @param filePath
     * @return
     */
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
}
