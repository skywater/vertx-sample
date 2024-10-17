
/**
 * Project Name: spbt-data-sync
 * File Name: Base256Util.java
 * @date 2024年4月22日 下午5:31:08
 * Copyright (c) 2024 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO <br/>
 * @date 2024年4月22日 下午5:31:08
 * @author jiangpq
 * @version
 */
@Slf4j
public class Base256Util {
	private static final Charset UTF8 = StandardCharsets.UTF_8;
	private static final List<Character> FILTERS = Arrays.asList('\r', '\n', '\t', ' ', ',', '，', '.', '。');
	private static final String CODE_TEXT = "天地玄黄，宇宙洪荒。日月盈昃，辰宿列张。\r\n"
			+ "寒来暑往，秋收冬藏。闰余成岁，律吕调阳。\r\n"
			+ "云腾致雨，露结为霜。金生丽水，玉出昆冈。\r\n"
			+ "剑号巨阙，珠称夜光。果珍李柰，菜重芥姜。\r\n"
			+ "海咸河淡，鳞潜羽翔。龙师火帝，鸟官人皇。\r\n"
			+ "始制文字，乃服衣裳。推位让国，有虞陶唐。\r\n"
			+ "吊民伐罪，周法殷汤。坐朝问道，垂拱平章。\r\n"   // 发 --> 法，避免下面重复
			+ "爱育黎首，臣伏戎羌。遐迩一体，率宾归王。\r\n"
			+ "鸣凤在竹，白驹食场。化被草木，赖及万方。\r\n"
			+ "盖此身发，四大五常。恭惟鞠养，岂敢毁伤。\r\n"
			+ "女慕贞洁，男效才良。知过必改，得能莫忘。\r\n"
			+ "罔谈彼短，靡恃己长。信使可覆，器欲难量。\r\n"
			+ "墨悲丝染，诗赞羔羊。景行维贤，克念作圣。\r\n"
			+ "德建名立，形端表正。空谷传声，虚堂习听。\r\n"
			+ "祸因恶积，福缘善庆。尺璧非宝，寸阴是竞。\r\n"
			+ "资父事君，曰严与敬。孝当竭力，忠则尽命。\r\n"
			+ "临深履薄，夙兴温凊。似兰斯馨，如松之盛。\r\n"
			+ "川流不息，渊澄取映。容止若思，言辞安定。";

	private static final char[] CODE_BOOK = new char[CODE_TEXT.length()];
	
	public static final Map<Character, Integer> CODE_MAP = new LinkedHashMap<>(CODE_TEXT.length());
	
	static {
		int k=0;
		int max = Byte.MAX_VALUE - Byte.MIN_VALUE;
		for(int i=0; i<CODE_TEXT.length(); i++) {
			if(k > max) {
				break;
			}
			char ch = CODE_TEXT.charAt(i);
			if(!FILTERS.contains(ch)) {
				CODE_MAP.put(ch, k);
				CODE_BOOK[k++] = ch;
			}
		}
//		System.out.println(CODE_MAP);
	}
	

	public static String cvt2Cn(String src) {
		return cvt2Cn(src.getBytes(UTF8));
	}
	public static String cvt2Cn(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length);
		for(int i=0; i<bytes.length; i++) {
			sb.append(CODE_BOOK[bytes[i] - Byte.MIN_VALUE]);
		}
		return sb.toString();
	}

	public static byte[] cvt4Cn(byte[] bytes) {
		return cvt4Cn(new String(bytes, UTF8));
	}
	public static byte[] cvt4Cn(String src) {
		byte[] bytes = new byte[src.length()];
		for(int i=0; i<src.length(); i++) {
			bytes[i] = (byte) (CODE_MAP.get(src.charAt(i)) + Byte.MIN_VALUE);
		}
		return bytes;
	}
	
	public static String compress(String str) {
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			DeflateCompressorOutputStream dcos = new DeflateCompressorOutputStream(baos);
			IOUtils.write(str, dcos);
			dcos.close();
//			return Base64Utils.encodeToString(baos.toByteArray());
			return cvt2Cn(baos.toByteArray());
		} catch (Exception e) {
			log.error("{} 压缩异常：", str, e);
			return null;
		}
	}
    
	public static String decompress(String str) {
		try(ByteArrayInputStream bais = new ByteArrayInputStream(cvt4Cn(str));  // Base64Utils.decodeFromString(str)
				DeflateCompressorInputStream dcis = new DeflateCompressorInputStream(bais);){
			return IOUtils.toString(dcis);
		} catch (IOException e) {
			log.error("{} 解压异常：", str, e);
			return null;
		}
	}
	
	public static void main(String[] args) {
//		String src = null;
//		String compress = compress(src);
//		System.out.println((null == src ? 0 : src.length()) + "," + compress.length() + "=" + compress);
//		System.out.println(decompress(compress));
		System.out.println(decompress(DocUtil.paste()));
	}
	
}

 