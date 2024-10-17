
/**
 * File Name: NetUtil.java
 * @date 2024年6月14日 下午3:16:45
 * Copyright (c) 2024 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterators;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO <br/>
 * @date 2024年6月14日 下午3:16:45
 * @author jiangpq
 * @version
 */
@Slf4j
public class NetUtil {
	public static final List<String> LOCAL_IPS = Arrays.asList("0:0:0:0:0:0:0:1", "127.0.0.1", "localhost");
	private static volatile String cachedIpAddress;


	public static String getIp() {
		if (StringUtils.isNotBlank(cachedIpAddress)) {
			return cachedIpAddress;
		}
		List<String> ipList = new ArrayList<>();
		List<String> otherList = new ArrayList<>();
		List<String> virtualIpList = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();			
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = netInterfaces.nextElement();
				if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
					continue;
				}
				String displayName = netInterface.getDisplayName().toLowerCase();
				String name = netInterface.getName().toLowerCase();
				Enumeration<InetAddress> ipAddresses = netInterface.getInetAddresses();
				Iterators.forEnumeration(ipAddresses).forEachRemaining(ipAddress -> {
					if (isPublicIpAddress(ipAddress)) {
						String publicIpAddress = ipAddress.getHostAddress();
						ipList.add(0, publicIpAddress);
					} else if (isLocalIpAddress(ipAddress)) {
						if(StringUtils.contains(displayName, "virtual") || StringUtils.contains(name, "docker")) {
							virtualIpList.add(ipAddress.getHostAddress());
						} else if(name.startsWith("ens")){
							ipList.add(ipAddress.getHostAddress());
						} else {
							otherList.add(ipAddress.getHostAddress());
						}
					}
				});
			}
		} catch (final SocketException ex) {
			throw new RuntimeException(ex);
		}
		if(otherList.size() > 0) {
			ipList.addAll(otherList);
		}
		if(virtualIpList.size() > 0) {
			ipList.addAll(virtualIpList);
		}
		cachedIpAddress = ipList.get(0);
		return cachedIpAddress;
	}

	private static boolean isPublicIpAddress(final InetAddress ipAddress) {
		return !ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress);
	}

	private static boolean isLocalIpAddress(final InetAddress ipAddress) {
		return ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress);
	}

	private static boolean isV6IpAddress(final InetAddress ipAddress) {
		return ipAddress.getHostAddress().contains(":");
	}
	
	public static String getReqIp(HttpServerRequest request, boolean isLog) {
		SocketAddress remoteAddress = request.remoteAddress();
		String hostAddress = StringUtils.defaultIfBlank(remoteAddress.host(), remoteAddress.hostAddress());
		MultiMap headers = request.headers();
		Map<String, String> map = new LinkedHashMap<String, String>();
        String xIp = headers.get("X-Real-IP");
        With.of(xIp).exec(e -> map.put("xRealIp", e));
        String xFor = headers.get("X-Forwarded-For");
        With.of(xFor).exec(e -> map.put("xForIp", e));
        if (StringUtils.isNotBlank(xFor) && !"unknown".equalsIgnoreCase(xIp)) {
            for(String elem : xFor.split(",")) {
            	elem = elem.trim();
            	if(!LOCAL_IPS.contains(elem) && StringUtils.isNotBlank(elem)) {
            		xIp = elem; // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            		break;
            	}
            }
        } else if (StringUtils.isBlank(xIp) || "unknown".equalsIgnoreCase(xIp)) {
        	xIp = With.of(headers.get("Proxy-Client-IP")).exec(e -> map.put("proxyIp", e)).get();
            xIp = StringUtils.defaultIfBlank(xIp, With.of(headers.get("WL-Proxy-Client-IP")).exec(e -> map.put("wlProxyIp", e)).get());
            xIp = StringUtils.defaultIfBlank(xIp, With.of(headers.get("HTTP_CLIENT_IP")).exec(e -> map.put("clientIp", e)).get());
            xIp = StringUtils.defaultIfBlank(xIp, With.of(headers.get("HTTP_X_FORWARDED_FOR")).exec(e -> map.put("hxForIp", e)).get());
            xIp = StringUtils.defaultIfBlank(xIp, With.of(hostAddress).map(e -> LOCAL_IPS.contains(e) ? getIp() : e).exec(e -> map.put("remoteIp", e)).get());
        }
        if(isLog) {
        	log.info("获取发起请求ip={}", Json.encode(map));
        }
        return xIp;	
	}

	public static String getRequestUrl(HttpServerRequest request) {
		String requestUrl = request.method() + ":" + request.absoluteURI();
		MultiMap queryParams = request.params();
		if(null != queryParams && !queryParams.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			queryParams.forEach(e -> {
				sb.append("&").append(e.getKey()).append("=").append(e.getValue());
			});
			requestUrl +=  "?" + sb.substring(1);
		}
		return requestUrl;
	}
}

 