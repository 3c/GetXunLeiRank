/**
 * Filename : ExcelUtil.java Author : CX Date : 2014-3-13
 * 
 * Copyright(c) 2011-2013 Mobitide Android Team. All Rights Reserved.
 */
package com.cx.thunder.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.cx.thunder.config.Config;
import com.cx.thunder.config.EnumVipLevel;
import com.cx.thunder.model.PersonModel;

/**
 * @author CX
 * 
 */
public class ExcelUtil {

	/**
	 * 
	 * 生成一个Excel文件
	 * 
	 * @param fileName
	 *            要生成的Excel文件名
	 * 
	 * @param list
	 * @param hashMapYesterday
	 */
	public static void writeExcel(String fileName, ArrayList<PersonModel> list,
			HashMap<Integer, PersonModel> hashMapYesterday) {
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
		cell.setCellValue("内部ID");
		cell.setCellStyle(style);
		cell = row.createCell((short) 1);
		cell.setCellValue("排名");
		cell.setCellStyle(style);
		cell = row.createCell((short) 2);
		cell.setCellValue("用户名");
		cell.setCellStyle(style);
		cell = row.createCell((short) 3);
		cell.setCellValue("经验");
		cell.setCellStyle(style);
		cell = row.createCell((short) 4);
		cell.setCellValue("省份");
		cell.setCellStyle(style);
		cell = row.createCell((short) 5);
		cell.setCellValue("是否是会员");
		cell.setCellStyle(style);
		cell = row.createCell((short) 6);
		cell.setCellValue("会员等级");
		cell.setCellStyle(style);
		cell = row.createCell((short) 7);
		cell.setCellValue("经验等级");
		cell.setCellStyle(style);
		cell = row.createCell((short) 8);
		cell.setCellValue("昨日增加经验");
		cell.setCellStyle(style);
		cell = row.createCell((short) 9);
		cell.setCellValue("名次变化");
		cell.setCellStyle(style);
		// 第五步，写入实体数据 实际应用中这些数据从数据库得到，

		for (int i = 0; i < list.size(); i++) {
			row = sheet.createRow((int) i + 1);
			PersonModel model = list.get(i);
			// 第四步，创建单元格，并设置值
			row.createCell((short) 0).setCellValue(model.innerno);
			row.createCell((short) 1).setCellValue(i + 1);
			row.createCell((short) 2).setCellValue(model.name);
			row.createCell((short) 3).setCellValue(model.exp);
			row.createCell((short) 4).setCellValue(model.region);
			row.createCell((short) 5).setCellValue(model.isvip == 1 ? "是" : "不是");
			EnumVipLevel vipLevel = EnumVipLevel.未知;
			int expAdded = 0;
			// System.out.println(nameArr[i]+" "+model.name);

			if (hashMapYesterday != null) {

				PersonModel modelYesterday = hashMapYesterday.get(model.innerno);
				
				if (hashMapYesterday != null && modelYesterday != null) {
					int rankDisplay=i+1;
					//排名变化
					if(rankDisplay==modelYesterday.rank_display){
						row.createCell((short) 9).setCellValue("-");
					}else if(rankDisplay>modelYesterday.rank_display){
						//排名降低了
						row.createCell((short) 9).setCellValue("↓ "+(rankDisplay-modelYesterday.rank_display));
					}else{
						row.createCell((short) 9).setCellValue("↑ "+(modelYesterday.rank_display-rankDisplay));
					}
					
					
					//经验值计算
					expAdded = model.exp - modelYesterday.exp;

					if (modelYesterday.isvip == 1) {

						int expAdd = model.exp - modelYesterday.exp;

						if (expAdd > 160) {
							//sb迅雷自己的经验值都搞不明白，部分用户可以今天经验值是0，明天是180
							if(modelYesterday.vip_level!=EnumVipLevel.未知){
								//这种sb的情况下，如果昨天不是未知，那继续采用昨天的等级。否则仍旧是未知
								vipLevel=modelYesterday.vip_level;
							}
						} else if (expAdd > 150) {
							vipLevel = EnumVipLevel.白金vip7;
						} else if (expAdd > 140) {
							vipLevel = EnumVipLevel.白金vip6;
						} else if (expAdd > 130) {
							vipLevel = EnumVipLevel.白金vip5;
						} else if (expAdd > 120) {
							vipLevel = EnumVipLevel.白金vip4或普通vip6;
						} else if (expAdd > 110) {
							vipLevel = EnumVipLevel.白金vip3或普通vip5;
						} else if (expAdd > 100) {
							vipLevel = EnumVipLevel.白金vip2或普通vip4;
						} else if (expAdd > 90) {
							vipLevel = EnumVipLevel.白金vip1或普通vip3;
						} else if (expAdd > 80) {
							vipLevel = EnumVipLevel.普通vip2;
						} else if (expAdd > 70) {
							vipLevel = EnumVipLevel.普通vip1;
						}

						// 如果通过经验值得出的vipLevel比昨天的要低，那就用昨天的

						if (vipLevel.compareTo(modelYesterday.vip_level) > 0) {
							vipLevel = modelYesterday.vip_level;
						}

					}

				}

			}

			row.createCell((short) 6).setCellValue(String.valueOf(vipLevel));
			row.createCell((short) 7).setCellValue(ExpUtil.getLevel(model.exp));
			row.createCell((short) 8).setCellValue(expAdded);

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

	/**
	 * 
	 * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
	 * 
	 * @param file
	 *            读取数据的源Excel
	 * 
	 * @param ignoreRows
	 *            读取数据忽略的行数，比喻行头不需要读入 忽略的行数为1
	 * 
	 * @return 读出的Excel中数据的内容
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 */

	public static String[][] readExcel(File file, int ignoreRows)

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
	 * @param str
	 *            要处理的字符串
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
