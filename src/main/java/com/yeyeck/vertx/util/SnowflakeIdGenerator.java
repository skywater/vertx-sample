
/**
 * Project Name: springboot
 * File Name: SnowflakeIdGenerator.java
 * @date 2021-1-715:52:52
 * Copyright (c) 2021 .com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法 <br/>
 * <pre>
 *  0 - [ 41 位时间戳 ] - [ 5位 datacenterId - 5位 workerId ] - [ 12 位序列号 ]
 *  0 - [1234567890 1234567890 1234567890 1234567890 0]- [12345 - 12345] - [123456789012]
 *  0 - [0000000000 0000000000 0000000000 0000000000 0]- [00000 - 00000] - [000000000000]
 * </pre>
 * 第一位为未使用(符号位表示正数)，接下来的41位为毫秒级时间(41位的长度可以使用69年)<br>
 * 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）<br>
 * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）<br>
 * 并且可以通过生成的id反推出生成时间,datacenterId和workerId <br>
 * 参考：http://www.cnblogs.com/relucent/p/4955340.html
 * @date 2021-1-7 15:52:52
 * @author jiangpeiquan
 * @version
 */
@Slf4j
public class SnowflakeIdGenerator {
    //================================================Algorithm's Parameter=============================================
    // 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
    private static final long startTime = 1288834974657L;
    // 机器id所占的位数
    private static final long workerIdBits = 5L;
    // 数据标识id所占的位数
    private static final long dataCenterIdBits = 5L;
    // 支持的最大机器id(十进制)，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    // -1 ^ (-32) = FFFF ^ FFE0 = FF ^ E0 = 1111 1111 ^ 1110 0000 = 11111 = 31
    private static final int maxWorkerId = -1 ^ (-1 << workerIdBits);
    // 支持的最大数据标识id - 31
    private static final int maxDataCenterId = -1 ^ (-1 << dataCenterIdBits);
    // 序列在id中占的位数
    private static final long sequenceBits = 12L;
    // 生成序列的掩码(12位所对应的最大整数值)，这里为4095 (0b111111111111=0xfff=4095)
    // 算法同上：-1 ^ (-4096) = FFFF FF ^ FFF0 00 = FFFF ^ F000 = FFF = 4095
    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);
    // 机器ID 左移位数 - 12 (即末 sequence 所占用的位数)
    private static final long workerIdMoveBits = sequenceBits;
    // 数据标识id 左移位数 - 17(12+5)
    private static final long dataCenterIdMoveBits = sequenceBits + workerIdBits;
    // 时间截向 左移位数 - 22(5+5+12)
    private static final long timestampMoveBits = sequenceBits + workerIdBits + dataCenterIdBits;
    
    //=================================================Works's Parameter================================================
    /**
     * 工作机器ID(0~31)
     */
    private int workerId;
    /**
     * 数据中心ID(0~31)
     */
    private int dataCenterId;
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;
    
    private static volatile SnowflakeIdGenerator snowflake = null;
    
    //===============================================Constructors=======================================================
    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param dataCenterId 数据中心ID (0~31)
     */
    public SnowflakeIdGenerator(int workerId, int dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("Worker Id can't be greater than " + maxWorkerId + " or less than 0");
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException("DataCenter Id can't be greater than " + maxDataCenterId + " or less than 0");
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }
    /**
     * 双重检测的单例模式
     *
     * @return
     */
    public static SnowflakeIdGenerator getInstance() {
        if(snowflake == null) {
            synchronized(SnowflakeIdGenerator.class) {
            	if(snowflake == null) {
	                int workerId;
	                int dataCenterId = getDatacenterId();
	                try {
	                    //第一次使用获取mac地址的
	                    workerId = getWorkerId(dataCenterId);
	                } catch (Exception e) {
	                	log.error("生成workId异常：", e);
	                    workerId = new Random().nextInt(maxWorkerId);
	                }
	                snowflake = new SnowflakeIdGenerator(workerId, dataCenterId);
            	}
            }
        }
        return snowflake;
    }
    
    public static long getId() {
    	return getInstance().nextId();
    }
    
    public static String getIdStr() {
    	return String.valueOf(getId());
    }
    
    // ==================================================Methods========================================================
    // 线程安全的获得下一个 ID 的方法
    public synchronized long nextId() {
        long timestamp = currentTime();
        // 如果当前时间小于上一次ID生成的时间戳: 说明系统时钟回退过 - 这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards.Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) { // 毫秒内序列溢出，即，序列 > 4095，阻塞到下一个毫秒,获得新的时间戳
                timestamp = blockTillNextMillis(lastTimestamp);
            }
        } else { // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - startTime) << timestampMoveBits) //
                | (dataCenterId << dataCenterIdMoveBits) //
                | (workerId << workerIdMoveBits) //
                | sequence;
    }
    
    // 阻塞到下一个毫秒 即 直到获得新的时间戳
    protected long blockTillNextMillis(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }
    
    // 获得以毫秒为单位的当前时间
    public static long currentTime() {
        return System.currentTimeMillis();
    }
    
    public static int getDatacenterId() {
    	int id = 0;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            log.info("ip={},hostName={}", ip.getHostAddress(), ip.getHostName());// 172.16.206.163,PF17DERE
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if(null == network) {
            	Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                while (en.hasMoreElements()) {
                	network = en.nextElement();
                    if (!network.isLoopback() && network.getHardwareAddress() != null) {
                    	log.info("重新获取network成功！");
                        break;
                    }
                }
            }
            id = 1;
            byte[] mac = network.getHardwareAddress();
            log.info("mac={}", mac);
            // MAC:70-C9-4E-F3-D4-8D，取出最后的2个
            id = (0x000000FF & mac[mac.length - 1]) | (0x0000FF00 & (mac[mac.length - 2] << 8));
            id = (id >>= 6) % (maxDataCenterId + 1);
        } catch (Exception e) {
        	log.info("获取datacenterId异常：{}", e);
        }
        return id;
    }
    
    public static int getWorkerId(int datacenterId) {
        StringBuffer mpid = new StringBuffer().append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        log.info("name={}", name); // 51960@PF17DERE
        if (!name.isEmpty()) { // GET jvmPid
            mpid.append(name.split("@")[0]);
        }
        // MAC + PID 的 hashcode 获取16个低位
        int val = mpid.toString().hashCode() & 0xffff;
        return val % (maxWorkerId + 1);
    }
    
    //====================================================Test Case=====================================================
    public static void main(String[] args) {
    	Runnable ru = () -> log.info("{}", getId());
    	Thread[] arr = new Thread[] {new Thread(ru),new Thread(ru),new Thread(ru),new Thread(ru)};
        for (Thread e : arr) {
            e.start();
        }
        System.out.println(new Date(1288834974657L));
    }
}