
package com.yeyeck.vertx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Base64Util;
import org.slf4j.helpers.MessageFormatter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.BaseEncoding;

import io.netty.handler.codec.base64.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO <br/>
 * @date 2020-10-12 11:48:44
 * @author jiangpeiquan
 * @version
 */
@Slf4j
public class StringUtil extends StringUtils {
	private static BiMap<String, String> chrMap = ImmutableBiMap.<String, String>builder()
			.put("(", "（")
			.put(")", "）")
			.put("[", "【")
			.put("]", "】")
			.build();
	
    /**
     * 转为中文全角符号 <br/>
     * @author jiangpeiquan
     * @param name
     * @param chrs
     * @return
     */
    public static String chsChar(String name, String... chrs) {
    	return replaceChr(name, chrMap, chrs);
    }
    
    /**
     * 转为英文半角符号 <br/>
     * @author jiangpeiquan
     * @param name
     * @param chrs
     * @return
     */
    public static String engChar(String name, String... chrs) {
    	return replaceChr(name, chrMap.inverse(), chrs);
    }
    
    public static String replaceChr(String name, Map<String, String> map, String... chrs) {
    	if(StringUtils.isBlank(name)) {
    		return name;
    	}
    	Collection<String> lst = ArrayUtils.isEmpty(chrs) ? map.keySet() : Arrays.asList(chrs);
    	for(String chr : lst) {
    		if(map.containsKey(chr)) {
    			name = name.replace(chr, map.get(chr));
    		}
    	}
    	return name;
    }
    
    /**
     * 解析关键字信息 <br/>
     * @author jiangpeiquan
     * @param word
     * @param startKey
     * @param endKey
     * @return
     */
    public static String cutCloseAll(String word, String startKey, String endKey) {
        return cutInfo(word, startKey, false, endKey, false);
    }
    public static String cutOpenAll(String word, String startKey, String endKey) {
        return cutInfo(word, startKey, true, endKey, true);
    }
    public static String cutOpenClose(String word, String startKey, String endKey) {
        return cutInfo(word, startKey, true, endKey, false);
    }
    public static String cutCloseOpen(String word, String startKey, String endKey) {
        return cutInfo(word, startKey, false, endKey, true);
    }
    /**
     * 截取信息，大部分可以被正则替换，保留中间（但正则有缺点，无法识别换行符！！） <br/>
     * @author jiangpeiquan
     * @param word
     * @param startKey
     * @param isStartOpen 一般是true，结果不包含起始关键字；false，包含；
     * @param endKey
     * @param isEndOpen 一般是true，结果不包含结束关键字；false，包含；
     * @return
     */
    public static String cutInfo(String word, String startKey, boolean isStartOpen, String endKey, boolean isEndOpen) {
        if(isBlank(word)) {
            return "";
        }
        int idx = startKey == null ? 0 : word.indexOf(startKey);
        startKey = startKey == null ? "" : startKey;
        word = word.substring(idx + (isStartOpen ? startKey.length() : 0), word.length());
        idx = endKey == null ? word.length() : word.indexOf(endKey);
        endKey = endKey == null ? "" : endKey;
        word = word.substring(0, idx >= 0 ? (isEndOpen ? idx : idx + endKey.length()) : word.length());
        return word;
    }
    
    /**
     * 获取关键词下标 <br/>
     * @author jiangpeiquan
     * @param word
     * @param startKey
     * @param endKey
     * @return
     */
    private static int[] indexs(String word, String startKey, String endKey) {
    	if(StringUtils.isBlank(word)) {
    		return null;
    	}
    	int[] ret = new int[2];
        startKey = startKey == null ? "" : startKey;
        int idx = word.indexOf(startKey);
        if(idx < 0) {
        	return null;
        }
        ret[0] = idx;
        word = word.substring(idx + startKey.length());
        endKey = endKey == null ? "" : endKey;
        idx = word.indexOf(endKey);
        if(idx < 0) {
        	return null;
        }
        ret[1] = idx + ret[0] + startKey.length();
    	return ret;
    }
    
    /**
     * 获取关键词 <br/>
     * @author jiangpeiquan
     * @param word
     * @param startKey
     * @param endKey
     * @return
     */
    public static String getKeyword(String word, String startKey, String endKey) {
    	String ret = "";
    	int[] idxs = indexs(word, startKey, endKey);
    	if(ArrayUtils.isEmpty(idxs) || idxs.length < 2) {
    		return ret;
    	}

        startKey = startKey == null ? "" : startKey;
    	return word.substring(idxs[0] + startKey.length(), idxs[1]);
    }
    
    /**
     * 根据关键词的下标替换 <br/>
     * @author jiangpeiquan
     * @param srcWord
     * @param startKey
     * @param endKey
     * @param keyword
     * @return
     */
    public static String replaceKeyword(String srcWord, String startKey, String endKey, String keyword) {
    	String ret = "";
    	int[] idxs = indexs(srcWord, startKey, endKey);
    	if(ArrayUtils.isEmpty(idxs) || idxs.length < 2) {
    		return ret;
    	}

        startKey = startKey == null ? "" : startKey;
    	return srcWord.substring(0, idxs[0] + startKey.length() + 1) + keyword + srcWord.substring(idxs[1]);
    }

	/**
	 * 处理空对象
	 * 
	 * @param strPar
	 * @return
	 */
	public static String nullStr(Object strPar) {
		if (strPar == null) {
			return "";
		}
		return strPar.toString();
	}
	public static String trimEmptyStr(Object strPar) {
		return nullStr(strPar).trim();
	}

	/**
	 * 解决编码问题，去掉换行 方法名称:replaceBlank 方法描述:
	 * 
	 * @param 短信内容
	 * @return 返回值描述 创建人：Administrator 创建时间：2015-11-10 下午03:25:52
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\\\t|\\\\r|\\\\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 清除字符串左右字符
	 * 
	 * @param s
	 * @param chars
	 * @return
	 */
	public static String trim(String s, char[] chars) {
		return trim(s, chars, null);
	}

	/**
	 * 清除字符串左侧指定范围内的字符，直到第一个非指定范围内的字符为止
	 * 
	 * @param s
	 * @param chars
	 * @return
	 */
	public static String trimLeft(String s, char[] chars) {
		return trim(s, chars, true);
	}

	/**
	 * 清除字符串右侧指定范围内的字符，直到第一个非指定范围内的字符为止
	 * 
	 * @param s
	 * @param chars
	 * @return
	 */
	public static String trimRight(String s, char[] chars) {
		return trim(s, chars, false);
	}

	/**
	 * 清除字符串左/右侧指定范围内的字符，直到第一个非指定范围内的字符为止
	 * 
	 * @param s
	 * @param chars
	 * @param isFromLeft
	 * @return
	 */
	public static String trim(String s, char[] chars, Boolean isFromLeft) {
		Set<String> set = new HashSet<>();
		for (char ch : chars) {
			set.add(String.valueOf(ch));
		}

		StringBuffer buf = new StringBuffer(s);
		boolean isFoundLeft = true;
		boolean isFoundRight = true;
		int left = 0;
		int right = buf.length() - 1;
		String strLeft = null;
		String strRight = null;
		for (; left < right;) {
			if (isFromLeft == null) {
				strLeft = isFoundLeft ? String.valueOf(buf.charAt(left)) : null;
				strRight = isFoundRight ? String.valueOf(buf.charAt(right)) : null;
				if (isFoundRight && set.contains(strRight)) {
					buf.deleteCharAt(right);
				} else {
					isFoundRight = false;
				}
				if (isFoundLeft && set.contains(strLeft)) {
					buf.deleteCharAt(left);
				} else {
					isFoundLeft = false;
				}
				if (!isFoundLeft && !isFoundRight) {
					break;
				}

				if (isFoundRight) {
					right = buf.length() - 1;
				}
			} else if (isFromLeft) {
				strLeft = isFoundLeft ? String.valueOf(buf.charAt(left)) : null;
				if (isFoundLeft && set.contains(strLeft)) {
					buf.deleteCharAt(left);
				} else {
					break;
				}
			} else {
				strRight = isFoundRight ? String.valueOf(buf.charAt(right)) : null;
				if (isFoundRight && set.contains(strRight)) {
					isFoundRight = true;
					buf.deleteCharAt(right);
					right--;
				} else {
					break;
				}
			}
		}

		return buf.toString();
	}
	
	/**
	 * 扩展方法 <br/>
	 * @author jiangpeiquan
	 * @param <T>
	 * @param value
	 * @param defaultStr
	 * @return
	 */
	public static <T> String defIfBlank(T value, String defaultStr) {
		if(null == value) {
			return defaultStr;
		}
		return StringUtils.defaultIfBlank(value.toString(), defaultStr);
	}
	
	/**
	 * 严格包含，即，都不能为空（null、""） <br/>
	 * @author jiangpeiquan
	 * @param src
	 * @param value
	 * @return
	 */
	public static boolean presentStrict (String src, String value) {
		if(isEmpty(src) || isEmpty(value)) {
			return false;
		}
		return src.contains(value);
	}
	
	/**
	 * 严格不包含 <br/>
	 * @author jiangpeiquan
	 * @param src
	 * @param value
	 * @return
	 */
	public static boolean absentStrict (String src, String value) {
		return !presentStrict(src, value);
	}


    public static String reduceInfo(String info, int length) {
    	return reduceInfo(info, length, false);
    }
    /**
     * 超过大小，截取信息 <br/>
     * @author jiangpeiquan
     * @param info
     * @param length
     * @return
     */
    public static String reduceInfo(String info, int length, boolean logSize) {
        if(StringUtils.isBlank(info) || info.length() <= length) {
            return info;
        }
        if(logSize) {
            log.info("原信息长度={},大小={}KB,压缩后={}", info.length(), info.getBytes().length/1024, Base256Util.compress(info));       
        }
        length = length >> 1;
        // 换为·号，因为.号会用于正则表达式
        String ret = info.substring(0, length).concat("······").concat(info.substring(info.length() - length, info.length()));
		return ret;
	}
    
    /**
     * 截取信息，默认最长3000 <br/>
     * @author jiangpeiquan
     * @param info
     * @return
     */
    public static String reduceInfo(String info) {
        return reduceInfo(info, 3000, false);
    }
    public static String reduceInfo(String info, boolean logSize) {
        return reduceInfo(info, 3000, logSize);
    }
    
    /**
     * 格式化字符串 <br/>
     * @author jiangpeiquan
     * @param pattern "{0}{1}" ，序号（无序）不能跳（1、3）；可以没有，则会忽视传参
     * @param arguments
     * @return
     */@SafeVarargs
	public static <T> String jdkFormat(String pattern, T ... arguments) {
        return MessageFormat.format(pattern, arguments);
    }
    
    /**
     * 格式化字符串，引用log方法 <br/>
     * @author jiangpeiquan
     * @param pattern "{}{}"
     * @param arguments
     * @return
     */@SafeVarargs
    public static <T> String format(String pattern, T ... arguments) {
    	return MessageFormatter.arrayFormat(pattern, arguments).getMessage();
    }
    /**
     * DecimalFormat格式化数字 <br/>
     * @author jiangpeiquan
     * @param pattern
     * @param num
     * @return
     */
    public static String formatNum(String pattern, Object num) {
    	String str = trimEmptyStr(num);
    	if(StringUtils.isBlank(str)) {
    		return str;
    	}
    	return new DecimalFormat(pattern).format(new BigDecimal(str));
    }
    public static String formatMoney(Object num) {
    	return formatNum("###,###.00", num);
    }
    
    /**
     * 返回异常堆栈信息 <br/>
     * @author jiangpeiquan
     * @param e
     * @return
     */
    public static String showException(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
		return StringUtil.reduceInfo(stringWriter.toString());
    }

    
    /**
     * 是否是英文字符 <br/>
     * @author jiangpeiquan
     * @param str
     * @return
     */
    public static boolean isEngChar(String str) {
    	if(isBlank(str)) {
    		return true;
    	}
    	return str.matches("[0-9a-zA-z ,;.() ]*");
    }
    public static boolean isEngNum(String str) {
    	if(isBlank(str)) {
    		return true;
    	}
    	return str.matches("[0-9a-zA-z ]*");
    }
    
	/**
	 * 判断字符串中是否只有中文
	 * @param str
	 * @return 
	 */
	public static boolean isOnlyChs(String str) {
    	if(isBlank(str)) {
    		return true;
    	}
		return str.matches("[\u3400-\u4db5\u4e00-\u9fd5 ]*");
	}
	/**
	 * 判断字符串中是否只有中文字符
	 * @param str
	 * @return 
	 */
	public static boolean isChsChar(String str) {
    	if(isBlank(str)) {
    		return true;
    	}
		return str.matches("[0-9\u3400-\u4db5\u4e00-\u9fd5（）【】，。；、 ]*");
	}

	/**
	 * 判断字符串中是否包含中文
	 * @param str
	 * @return 
	 */
	public static boolean containChs(String str) {
		Pattern p = Pattern.compile("[\u3400-\u4db5\u4e00-\u9fd5 ]");
		Matcher m = p.matcher(str);
		return m.find();
	}
	
	// 找出汉字的最大值 0x9fd5
//	for(int i = 0x9fbf; i<0x9FFe;i++ ) {System.out.println(Integer.toHexString(i) + "," + (char)(i));}
	// 汉字最小值 [0x3400, 0x4db5], [0x4e00, 0x9fd5]
    public static boolean isAllChs(String str) {
    	char c = 0;
    	for(int i=0; i<str.length(); i++) {
    		c = str.charAt(i);
//    		log.info("{}={}", c, Integer.toHexString(c));  // 网络资料不一样：[\u4e00, \u9fa5、\u9fbf]
    		if (c != ' ' || c < 0x3400 ||  c > 0x9fd5 || (c > 0x4db5 && c < 0x4e00)){
            	return false;
            }
        }
    	return true;
    }
    
	/**
	 * 解析字符串，获取第N个，负数为倒数N个 <br/>
	 * @author jpq
	 * @param value
	 * @param idx
	 * @return
	 */
	public static String splitIdx(String value, String splitStr, int idx) {
		if(isBlank(value)) {
			return "";
		}
		String[] split = value.split(splitStr);
		return split[idx >= 0 ? idx : split.length+idx];
	}

	/**
	 * 将字符串转为map；支持格式（分隔符为&；; 赋值符号为=:）如下：<br>
	 * a=1;b:2；c=3&d：4
	 * @param str
	 * @return 
	 */
	public static Map<String, String> toMap(String str) {
		Map<String, String> map = new HashMap<>();
		if(StringUtils.isBlank(str)) {
			return map;
		}
		String[] arr = str.split(";|；|&");
		String[] tmp = null; 
		for(String elem : arr) {
			if(StringUtils.isBlank(elem)) {
				continue;
			}
			tmp = elem.split("=|:|：");
			map.put(tmp[0], tmp[1]);
		}
		return map;
	}
	
	/**
	 * 追加方法，如果src为空，则直接返回 append <br/>
	 * @author jpq
	 * @param src
	 * @param chr
	 * @param append
	 * @return
	 */
	public static String append(String src, String chr, String append) {
		return isBlank(src) ? append : src + chr + append;
	}
	
	/**
	 * 处理为驼峰字段 <br/>
	 * @author jiangpq
	 * @param prefix
	 * @param name
	 * @param suffix
	 * @return
	 */
	public static String formatField(String prefix, String name, String suffix) {
		prefix = null == prefix ? "" : prefix;
        suffix = null == suffix ? "" : suffix;
		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1) + suffix;
	}
	
	/**
	 * 互相包含 <br/>
	 * @author jiangpq
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean containBoth(String str1, String str2) {
		return contains(str1, str2) || contains(str2, str1);
	}
    
	public static String compress(String str) {
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			DeflateCompressorOutputStream dcos = new DeflateCompressorOutputStream(baos);
			IOUtils.write(str, dcos);
			dcos.close();
			return BaseEncoding.base64().encode(baos.toByteArray());
		} catch (Exception e) {
			log.error("{} 压缩异常：", str, e);
			return null;
		}
	}
    
	public static String decompress(String str) {
		try(ByteArrayInputStream bais = new ByteArrayInputStream(BaseEncoding.base64().decode(str));
				DeflateCompressorInputStream dcis = new DeflateCompressorInputStream(bais);){
			return IOUtils.toString(dcis);
		} catch (IOException e) {
			log.error("{} 解压异常：", str, e);
			return null;
		}
	}
	
    public static void main(String[] args) throws IOException{
    	System.out.println(engChar("三点（零）开始看到（二位）"));
    	System.out.println(chsChar("三点(零)开始看到(二位)"));

		System.out.println(getKeyword("\"ip\":\"123\"", "\"ip\":\"", "\"")); // 123
		System.out.println(replaceKeyword("\"ip\":\"123\"", "\"ip\":\"", "\"", "234567")); // "ip":"1234567"
		
		System.out.println(isEngChar("三点（零）开始看到（二位）"));
		System.out.println(isEngChar("a Z, 6"));
		System.out.println(isEngChar("adf4 "));
		System.out.println(isEngChar("a Z,1 3"));
		System.out.println(isEngChar("adf"));
		System.out.println("------中文字符-----");
		System.out.println(isChsChar("三点（零）开始看到（二位） 龥鿎 龤 "));
		System.out.println(isOnlyChs("三点零开始看 龥 龿到鿎二位") + "," + isAllChs("三点零开始看到龿龤鿎二位"));
		// \u4e00-\u9fa5 \u9fbf
		System.out.println("\u4e00 \u4e09 \u96f6 \u9fa5 \u9fa4 \u9fbf \u9fce");
		System.out.println("龥");

		System.out.println(formatNum("###,###.000", 12345678.9056345));
		System.out.println(formatNum("###,###.000", "12345678.9056345"));
		System.out.println(formatNum("###,###.000", null));
		

        System.out.println(splitIdx("http://10.0.218.162:5656/api/Authentication\\1001", "\\\\|/", -2));
        System.out.println(splitIdx("http://10.0.218.162:5656/api/Authentication\\1001", "\\\\|/", -1));

        // 序号（无序）不能跳（1、3）；可以没有，则会忽视传参
        System.out.println(jdkFormat("顶顶顶", "呱呱呱"));
        System.out.println(jdkFormat("顶顶顶{0}", "呱呱呱"));
        System.out.println(jdkFormat("{1}顶顶顶{0}", "呱呱呱", "哈哈哈"));
        System.out.println(jdkFormat("{1}顶顶顶{0}", "呱呱呱", null));
        System.out.println(jdkFormat("{1}顶顶顶{0}", "呱呱呱"));
        System.out.println(toMap("a=1;b:2；c=3&d：4"));
    }
}