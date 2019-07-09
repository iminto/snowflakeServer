package iminto.github.io;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 雪花算法，带还原方法
 * 参考：https://segmentfault.com/a/1190000011282426
 */
public class SnowFlakeIdWorker {
    private long workerId;
    private long dataCenterId;
    private long sequence;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");


    public SnowFlakeIdWorker(long workerId, long dataCenterId, long sequence){
        // sanity check for workerId
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0",maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenter Id can't be greater than %d or less than 0",maxDataCenterId));
        }
//        System.out.printf("worker starting. timestamp left shift %d, dataCenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
//                timestampLeftShift, dataCenterIdBits, workerIdBits, sequenceBits, workerId);

        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
        this.sequence = sequence;
    }

    private long twepoch = 1561910400000L;//起始时间戳，用于用当前时间戳减去这个时间戳，算出偏移量,2019-07-01 00:00:00

    private long workerIdBits = 5L;//workerId占用的位数：5
    private long dataCenterIdBits = 5L;//dataCenterId占用的位数：5
    public static long maxWorkerId = 31;// workerId可以使用的最大数值：31 -1L ^ (-1L << workerIdBits)
    public static long maxDataCenterId = 31;//dataCenterId可以使用的最大数值：31
    private long sequenceBits = 12L;//序列号占用的位数：12

    private long workerIdShift = sequenceBits;
    private long dataCenterIdShift = sequenceBits + workerIdBits;// 12+5 = 17
    private long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits; //12+5+5 = 22
    private long sequenceMask = -1L ^ (-1L << sequenceBits);//4095

    private long lastTimestamp = -1L;

    public long getWorkerId(){
        return workerId;
    }

    public long getDataCenterId(){
        return dataCenterId;
    }

    public long getTimestamp(){
        return System.currentTimeMillis();
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        return ((timestamp - twepoch) << timestampLeftShift) |
                (dataCenterId << dataCenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen(){
        return System.currentTimeMillis();
    }

    private long getBitMask(Long bit){
        return -1L ^ -1L << bit;
    }

    public void parseInfo(long genId) {
        String idStr=Long.toBinaryString(genId);
        System.out.println(idStr);
        String sequence = idStr.substring((int)(idStr.length()-workerIdShift), idStr.length());
        System.out.println("recoverSequence="+sequence+"="+Integer.valueOf(sequence,2));
        long workIdShift = genId >>> workerIdShift;
        long recoverWorkId = workIdShift & getBitMask(workerIdBits);
        System.out.printf("recoverWorkId=%d, binary=%s \r\n", recoverWorkId, Long.toBinaryString(recoverWorkId));
        long dataCenterShift = genId >>> dataCenterIdShift;
        long recoverDataCenterId = dataCenterShift & getBitMask(dataCenterIdBits);
        System.out.printf("recoverDataCenterId=%d, binary=%s\n", recoverDataCenterId, Long.toBinaryString(recoverDataCenterId));
        long recoverTime = genId >>> timestampLeftShift;
        System.out.printf("recoverTime=%d, binary=%s \n", recoverTime, Long.toBinaryString(recoverTime));
        long timestamp = twepoch + recoverTime;
        System.out.printf("timestamp=%s\n", sdf.format(new Date(timestamp)));
    }

    //---------------测试---------------
    public static void main(String[] args) {
//        SnowFlakeIdWorker worker = new SnowFlakeIdWorker(13L,7L,100L);
//        for (int i = 0; i < 10; i++) {
//            System.out.println(worker.nextId());
//        }
//        worker.parseInfo(3127342709788674L);
    }
}
