/**
 * File Name: RegexUtil.java
 * @date 2019年1月16日下午2:09:20
 * Copyright (c) 2019 .com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * @date 2019年1月16日 下午2:09:20
 * @author jiangpeiquan
 * @version
 */
@Slf4j
public class RegexUtil {
	private RegexUtil() {
	}
    

    /**
     * 循环获取所有匹配结果的所有分组<br/>
     * @param src
     * @param regex
     * @return
     * @date: 2017年11月13日 下午3:46:57 <br/>
     * @since
     */
    public static List<String> getAllMatchGroups(String src, String regex) {
        List<String> result = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(src);
        while(m.find()) { // 循环获取
            for(int i=1; i<=m.groupCount(); i++) {
                result.add(m.group(i));
            }
        }
        log.info("getAllMatchGroups匹配结果={}", result);
        return result;
    }

    /**
     * 正则匹配 <br/>
     * @author jiangpq
     * @param src
     * @param regex
     * @return
     */
    public static boolean match(String src, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(src);
    	return m.matches();
    }
}
