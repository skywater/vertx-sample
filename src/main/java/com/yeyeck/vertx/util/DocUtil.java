
/**
 * Project Name: fast-flowable-pure-api
 * File Name: DocUtil.java
 * @date 2021年8月31日 上午8:55:34
 * Copyright (c) 2021 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.core.io.InputStreamSource;

import com.yeyeck.vertx.model.BaseKeyVal;
import com.yeyeck.vertx.model.BaseKeyValType;

/**
 * TODO <br/>
 * @date 2021年8月31日 上午8:55:34
 * @author jpq
 * @version
 */
public class DocUtil {
    protected static final Logger log = LoggerFactory.getLogger(DocUtil.class);
	
	public static final String ORCL_TMPT = "declare\r\n"
			+ "v_cnt number;\r\n"
			+ "begin\r\n"
			+ "%s\r\n"
//			+ "select count(*) into v_cnt from user_tab_columns where table_name = upper('{}');\r\n"
			+ "if v_cnt > 0 then\r\n"
			+ "%s\r\n"
//			+ "	DBMS_OUTPUT.PUT_LINE('{} 表已经存在');\r\n"
			+ "else\r\n"
			+ "	execute immediate '{}';\r\n"
			+ "end if;\r\n"
			+ "exception\r\n"
			+ "WHEN others THEN\r\n"
			+ "  ROLLBACK;\r\n"
			+ "  dbms_output.put_line ('创建{}执行失败,错误码:' || SQLCODE || ';错误信息:' || substr(sqlerrm, 1, 160));\r\n"
			+ "end;";
	
	public static final String CREATE_TAB_TMPT = String.format(ORCL_TMPT, 
			"select count(*) into v_cnt from user_tab_columns where table_name = upper('{}');",
			"	DBMS_OUTPUT.PUT_LINE('{} 表已经存在');");
	
	public static final String ALTER_TAB_TMPT = String.format(ORCL_TMPT, 
			"select count(*) into v_cnt from user_tab_columns where table_name = upper('{}') and column_name = upper('{}');",
			"	DBMS_OUTPUT.PUT_LINE('{} 表 {} 字段已经存在');");
	
	public static final String CREATE_IDX_TMPT = String.format(ORCL_TMPT, 
			"select count(*) into v_cnt from user_indexes where table_name = upper('{}') and index_name = upper('{}');",
			"	DBMS_OUTPUT.PUT_LINE('{} 表 {} 索引已经存在');");
	
	// oracle索引命名数据库唯一，所以不必指定表
	public static final String ALTER_IDX_TMPT = String.format(ORCL_TMPT, 
			"select CASE WHEN count(*) > 0 THEN 0 ELSE 1 END into v_cnt from user_indexes where index_name = upper('{}');",
			"	DBMS_OUTPUT.PUT_LINE('{}索引不存在');");
	
	public static final String CREATE_SEQ_TMPT = String.format(ORCL_TMPT, 
			"select count(*) into v_cnt from USER_SEQUENCES where SEQUENCE_NAME = upper('{}');",
			"	DBMS_OUTPUT.PUT_LINE('{}序列不存在');");

	public static final String CREATE_TAB = "CREATE TABLE"; // CREATE TABLE "FASTMGR"."T_CREDIT_RATING"(
	public static final String ALTER_TAB = "ALTER TABLE"; // alter table T_TRADE_PARSE add BATCH_NO VARCHAR2(50);
	public static final String CREATE_IDX = "CREATE INDEX"; // CREATE INDEX idx_orgName_remark ON T_CREDIT_RATING(org_name,ENABLE);
	public static final String CREATE_UNIQUE = "CREATE UNIQUE"; // CREATE UNIQUE INDEX idx_orgName_remark ON T_CREDIT_RATING(org_name,ENABLE);
	public static final String CREATE_UNIQUE_IDX = CREATE_UNIQUE + " INDEX";
	public static final String ALTER_IDX = "ALTER INDEX"; // alter index idx_orgName_remark rename to idx_orgName_enable;
	public static final String CREATE_SEQ = "CREATE SEQUENCE"; // CREATE SEQUENCE "FLOWABLE"."ACT_EVT_LOG_SEQ" MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 CACHE 20
	public static final String COMMENT_ON = "COMMENT ON"; // COMMENT ON COLUMN "FLOWABLE"."FLW_BIZ_CONFIG"."FLW_KEY" IS ''流程图唯一key''
	
	
	/**
	 * 转义，并且去掉结尾分号 <br/>
	 * @author jpq
	 * @param sql
	 * @return
	 */
	public static String dealSql(String sql) {
		sql = sql.trim().replace("'", "''");
		if(sql.endsWith(";")) {
			sql = sql.substring(0, sql.length()-1);
		}
		return sql;
	}
	
	public static BaseKeyValType<String, String, String> convertSqlSeg(String sqlSeg) {
		return convertSqlSeg(sqlSeg, false);
	}
	
	/**
	 * 解析sql，返回<key=表名，value=字段名或索引名，type=DDL类型模板> <br/>
	 * @author jpq
	 * @param sqlSeg
	 * @return
	 */
	public static BaseKeyValType<String, String, String> convertSqlSeg(String sqlSeg, boolean endWithSlash) {
		String sql = sqlSeg.replace("\t", " ").replaceAll(" +", " ").trim();
		String[] arr = sql.split(" ");
		String type = (arr[0] + " " + arr[1]).toUpperCase();
		BaseKeyValType<String, String, String> ret = new BaseKeyValType<>();
		String headInfo = arr[2].toUpperCase();

		if(COMMENT_ON.equals(type)) { // COMMENT ON COLUMN "FLOWABLE"."FLW_BIZ_CONFIG"."FLW_KEY" IS ''流程图唯一key''
			ret.setType(sqlSeg + (endWithSlash ? "\\\n" : ""));
			return ret;
		}

		sql = dealSql(sqlSeg);
		if(CREATE_TAB.equals(type)) { // CREATE TABLE "FASTMGR"."T_CREDIT_RATING"(
			headInfo = headInfo.contains("(") ? headInfo.substring(0, headInfo.indexOf("(")) : headInfo;
			headInfo = headInfo.contains(".") ? headInfo.split("\\.")[1] : headInfo;
			ret.setKey(headInfo.trim().replace("\"", ""));
			ret.setType(StringUtil.format(CREATE_TAB_TMPT, ret.getKey(), ret.getKey(), sqlSeg));
		} else if(CREATE_IDX.equals(type)) { // CREATE INDEX idx_orgName_remark ON T_CREDIT_RATING(org_name,ENABLE);
			ret.setValue(headInfo);
			headInfo = arr[4].toUpperCase();
			headInfo = headInfo.contains("(") ? headInfo.substring(0, headInfo.indexOf("(")) : headInfo;
			ret.setKey(headInfo);
			ret.setType(StringUtil.format(CREATE_IDX_TMPT, ret.getKey(), ret.getValue(), ret.getKey(), ret.getValue(), sqlSeg));
		}  else if(CREATE_UNIQUE.equals(type) && CREATE_UNIQUE_IDX.equals(type + " " + arr[2].toUpperCase())) {
			// CREATE UNIQUE INDEX idx_orgName_remark ON T_CREDIT_RATING(org_name,ENABLE);
			ret.setValue(arr[3].toUpperCase());
			headInfo = arr[5].toUpperCase();
			headInfo = headInfo.contains("(") ? headInfo.substring(0, headInfo.indexOf("(")) : headInfo;
			ret.setKey(headInfo);
			ret.setType(StringUtil.format(CREATE_IDX_TMPT, ret.getKey(), ret.getValue(), ret.getKey(), ret.getValue(), sqlSeg));
		} else if(ALTER_TAB.equals(type)) { // alter table T_CREDIT_RATING add BATCH_NO VARCHAR2(50);
			ret.setKey(headInfo);
			ret.setValue(arr[4].toUpperCase());
			ret.setType(StringUtil.format(ALTER_TAB_TMPT, ret.getKey(), ret.getValue(), ret.getKey(), ret.getValue(), sqlSeg));
		} else if(ALTER_IDX.equals(type)) { // alter index idx_orgName_remark rename to idx_orgName_enable;
			headInfo = headInfo.contains(".") ? headInfo.split("\\.")[1] : headInfo;
			ret.setValue(headInfo);
			ret.setType(StringUtil.format(ALTER_IDX_TMPT, ret.getValue(), ret.getValue(), sqlSeg));
		} else if(CREATE_SEQ.equals(type)) { // CREATE SEQUENCE "FLOWABLE"."ACT_EVT_LOG_SEQ"
			headInfo = headInfo.contains(".") ? headInfo.split("\\.")[1] : headInfo;
			ret.setValue(headInfo.trim().replace("\"", ""));
			ret.setType(StringUtil.format(CREATE_SEQ_TMPT, ret.getValue(), ret.getValue(), sqlSeg));
		} else if(COMMENT_ON.equals(type)) { // COMMENT ON COLUMN "FLOWABLE"."FLW_BIZ_CONFIG"."FLW_KEY" IS ''流程图唯一key''
			ret.setType(sqlSeg + ";");
		} else {
			log.error("不支持的DDL类型：{}", sql);
			return null;
		}
		ret.setType(ret.getType() + "\n" + (endWithSlash ? "\\\n" : ""));
		return ret;
	}

	
	/**
	 * 分解出<注释、sql> <br/>
	 * @author jpq
	 * @param sql
	 * @return
	 */
	private static BaseKeyVal<String, String> parseSqlSeg(String sql) {
		if(StringUtils.isBlank(sql)) {
			return null;
		}
		StringBuilder commentSb = new StringBuilder();
		StringBuilder sqlSb = new StringBuilder();
		String[] arr = sql.split("\n");
		for(String elem : arr) {
			elem = elem.trim();
			if(elem.startsWith("--")) {
				commentSb.append(elem).append("\n");
			} else {
				sqlSb.append(elem).append("\n");
			}
		}
		return new BaseKeyVal<String, String>(commentSb.toString(), sqlSb.toString());
	}

	public static String formatOrclDdlFile(String sql) {
		return formatOrclDdlFile(sql, false);
	}
	public static String formatOrclDdlFile(String sql, boolean endWithSlash) {
		if(StringUtils.isBlank(sql)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		String[] arr = sql.trim().split(";\n|;\r\n");
		BaseKeyVal<String, String> sqlSeg = null;
		for(String elem : arr) {
			if(StringUtils.isBlank(elem)) {
				sb.append(elem);
				continue;
			}
			sqlSeg = parseSqlSeg(elem);
			if(StringUtils.isNotEmpty(sqlSeg.getKey())) {
				sb.append("\n").append(sqlSeg.getKey());
			}
			if(StringUtils.isBlank(sqlSeg.getValue())) {
				continue;
			}
			elem = sqlSeg.getValue();
			try {
				BaseKeyValType<String, String, String> parseSql = convertSqlSeg(elem, endWithSlash);
				if(null != parseSql) {
					sb.append(parseSql.getType());
				} else {
					log.warn("不支持的格式", elem);
				}
			}catch (Exception e) {
				log.error("{}解析异常：", elem, e);
				throw new RuntimeException(elem + "解析异常");
			}
		}
		return sb.toString();
	}
	

//	public static String formatOrclDdlFile(InputStreamSource file) {
//		return formatOrclDdlFile(file, false);
//	}
//	public static String formatOrclDdlFile(InputStreamSource file, boolean endWithSlash) {
//		try {
//			return formatOrclDdlFile(IOUtils.toString(file.getInputStream()));
//		} catch (IOException e) {
//			log.error("读取文件异常：", e);
//		}
//		return null;
//	}
//	public static String formatOrclDdlFile(File file) {
//		return formatOrclDdlFile(file, false);
//	}
//	public static String formatOrclDdlFile(File file, boolean endWithSlash) {
//		try {
//			return formatOrclDdlFile(FileUtil.readFileToString(file));
//		} catch (IOException e) {
//			log.error("读取文件异常：", e);
//		}
//		return null;
//	}
//	public static String formatOrclDdlFilePath(String filePath) {
//		return formatOrclDdlFilePath(filePath, false);
//	}
//	public static String formatOrclDdlFilePath(String filePath, boolean endWithSlash) {
//		return formatOrclDdlFile(new File(filePath), false);
//	}
	
    /**
     * 把文本设置到剪贴板（复制）
     */
    public static void copy(String text) {
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
    }

    /**
     * 从剪贴板中获取文本（粘贴）
     */
    public static String paste() {
    	String text = "";
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // 获取剪贴板中的内容
        Transferable trans = clipboard.getContents(null);
        if (trans == null) {
        	return text;
        }
        // 判断剪贴板中的内容是否支持文本
        if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                // 获取剪贴板中的文本内容
            	text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                return text;
            } catch (Exception e) {
                log.error("paste异常：", e);
            }
        }

        return text;
    }
	
	public static void main(String[] args) throws IOException {
		String createTab = "CREATE TABLE \"FASTMGR\".\"T_CREDIT_RATING\" (	\r\n"
				+ "\"ID\" NUMBER(22,0) NOT NULL ENABLE, \r\n"
				+ "\"TYPE\" NUMBER(2, 0) DEFAULT 0 NOT NULL ENABLE, \r\n"
				+ "\"BOND_CODE\" VARCHAR2(63 BYTE), \r\n"
				+ "\"BOND_SNAME\" VARCHAR2(63 BYTE), \r\n"
				+ "\"BOND_SPY\" VARCHAR2(63 BYTE), \r\n"
				+ "\"ORG_NAME\" VARCHAR2(63 BYTE), \r\n"
				+ "\"ORG_SNAME\" VARCHAR2(63 BYTE), \r\n"
				+ "\"ORG_SPY\" VARCHAR2(63 BYTE), \r\n"
				+ "\"ORG_PY\" VARCHAR2(127 BYTE), \r\n"
				+ "\"SOCIAL_CREDIT_CODE\" VARCHAR2(31 BYTE), \r\n"
				+ "\"IN_TIME\" TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL ENABLE,\r\n"
				+ "\"RATING_DATE\" DATE, \r\n"
				+ "\"EXPIRY_DATE\" DATE, \r\n"
				+ "\"CREDIT_RATING\" VARCHAR2(31 BYTE), \r\n"
				+ "\"CREDIT_RATING_NUM\" NUMBER(22, 0), \r\n"
				+ "\"SOURCE_TYPE\" VARCHAR2(31 BYTE) DEFAULT 'HX_INNER_RATING' NOT NULL ENABLE,\r\n"
				+ "\"RATING_METHOD\" NUMBER(2, 0) DEFAULT 0 NOT NULL ENABLE,\r\n"
				+ "\"ENABLE\" NUMBER(2,0) DEFAULT 1 NOT NULL ENABLE, \r\n"
				+ "\"CREATE_BY\" NUMBER(22,0) DEFAULT 0 NOT NULL ENABLE, \r\n"
				+ "\"CREATE_TIME\" TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL ENABLE, \r\n"
				+ "\"UPDATE_BY\" NUMBER(22,0) DEFAULT 0 NOT NULL ENABLE, \r\n"
				+ "\"UPDATE_TIME\" TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL ENABLE, \r\n"
				+ "  CONSTRAINT \"T_CREDIT_RATING_PK\" PRIMARY KEY (\"ID\"));";
		String alterTab = "alter table T_CREDIT_RATING add BATCH_NO VARCHAR2(50);";
		String createIdx = "CREATE INDEX idx_orgName_remark1 ON T_CREDIT_RATING(org_name,ENABLE,org_sname)";
		String alterIdx = "ALTER INDEX idx_orgName_enable REBUILD;";
//		System.out.println(formatOrclDdlFile(createTab));
//		System.out.println(formatOrclDdlFile(alterTab));
//		System.out.println(formatOrclDdlFile(createIdx));
//		System.out.println(formatOrclDdlFile(alterIdx));
		
		String filePath = "F:\\work\\hx168\\project\\dev\\sale_sql\\1.0.7\\DDL\\FLOWABLE_V.1.0.7_JIANGPQ_DDL_备份（不用执行）.sql";
//		System.out.println(formatOrclDdlFilePath(filePath));
//		FileUtil.writeStringToFile(new File(filePath + ".format"), formatOrclDdlFilePath(filePath));
	}
}