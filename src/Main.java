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
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main {

	static ArrayList<PersonModel> list = new ArrayList<PersonModel>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
//		read();
		write();

	}
	
	static int STANDER=210000;
	
	static int STANDER_RANK=700;

	public static void read() {
	
		 new Thread(new Runnable() {
			
			@Override
			public void run() {try {
				
				String url = "http://dynamic.i.xunlei.com/ajax?c=site&a=topList&type1=1&type2=0&ext=";
				 ArrayList<PersonModel> listCache = new ArrayList<PersonModel>();
					int time = 0;
					for (int i = 1; i < 35; i++) {
						
						time=(int) (Math.random()*10000);
						String requestUrl = url + i + "&cachetime="
								+ System.currentTimeMillis();
//						String requestUrl = url + i ;
						System.out.println(requestUrl);
						String content = sendGet(requestUrl);
						PersonListModel listModel = new Gson().fromJson(content,
								PersonListModel.class);
						System.out.println(listModel.data.get(0).region+" : "+ listModel.data.size());
						listCache.addAll(listModel.data);
						Thread.sleep(time);
						
						if(i%10==0){
							Thread.sleep(10000);
						}
					
					}
					Collections.sort(listCache, new ComparatorUser());
					
//					for(int i=0;i<listCache.size();i++){
//						if(listCache.get(i).exp>STANDER){
//							list.add(listCache.get(i));
//						}else{
//							break;
//						}
//					}
					
					for(int i=0;i<STANDER_RANK;i++){
						list.add(listCache.get(i));
					}
					
					
					
					System.out.println(list.size());

					String content = new Gson().toJson(list);
					writeFileToSD("text.txt", content);
				
				
				
			
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			}
		}).start();

	}

	public static void write() {
		String content = getJsonFromAssets("d:/test/text.txt");

		Type type = new TypeToken<List<PersonModel>>() {
		}.getType();
		list = new Gson().fromJson(content, type);
		System.out.println(list.size());
		writeExcel();

	}

	/**
	 * 生成一个Excel文件
	 * 
	 * @param fileName
	 *            要生成的Excel文件名
	 */
	public static void writeExcel() {
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

		// 第五步，写入实体数据 实际应用中这些数据从数据库得到，

		for (int i = 0; i < list.size(); i++) {
			row = sheet.createRow((int) i + 1);
			PersonModel model = list.get(i);
			// 第四步，创建单元格，并设置值
			row.createCell((short) 0).setCellValue(i + 1);
			row.createCell((short) 1).setCellValue(model.name);
			row.createCell((short) 2).setCellValue(model.exp);
			row.createCell((short) 3).setCellValue(model.region);
			row.createCell((short) 4).setCellValue(
					model.isvip == 1 ? "是" : "不是");
		}
		// 第六步，将文件存到指定位置
		try {
			FileOutputStream fout = new FileOutputStream("D:/test/text.xls");
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeFileToSD(String fileName, String content) {
		try {
			// String fileName =mfileName ;
			String pathName = "d:/test/";
			// fileName += ".txt";
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
			Reader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
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
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
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
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			// for (String key : map.keySet()) {
			// System.out.println(key + "--->" + map.get(key));
			// }
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
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

}
