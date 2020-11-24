package com.daoqidlv.filespilt.single.forkjoin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.DeviceResultInfo;
import com.alipay.api.request.AlipayUserAccountDeviceInfoQueryRequest;
import com.daoqidlv.filespilt.Util;
import com.daoqidlv.filespilt.util.AlipayUtil;
import com.daoqidlv.filespilt.util.StringBuilderPlus;

/**
 * 文件写入任务类，记录每个文件写入任务信息，且完成文件写入。
 *
 * @author daoqidelv
 * @CreateDate 2017年5月4日
 */
public class ForkFileWriteTask extends RecursiveTask<Integer> {

    private static final long serialVersionUID = 1L;
    /**
     * 文件所在路径
     */
    private String fileDir;
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件内容
     */
    private List<String> fileContent;

    /**
     * 文件大小
     */
    private int fileSize;

    public ForkFileWriteTask(String fileDir, String fileName, List<String> fileContent, int fileSize) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.fileDir = fileDir;
        this.fileSize = fileSize;
    }


    @Override
    protected Integer compute() {
        File file = new File(Util.genFullFileName(this.fileDir, this.fileName));
        BufferedWriter bw = null;
        AtomicInteger writtenSize = new AtomicInteger();

        String appId = AlipayUtil.APPID;
        String privateKey = AlipayUtil.PRIVATE_KEY;
        String publicKey = AlipayUtil.PUBLIC_KEY;
        String method = AlipayUtil.METHOD;

        AlipayClient alipayClient = new DefaultAlipayClient(method, appId, privateKey, "json", "utf-8", publicKey, "RSA2");

        try {
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            int i = 1;
            StringBuilderPlus sb = new StringBuilderPlus();
            for (String lineContent : fileContent) {
                // TODO: 2020-11-13 请求支付宝接口
                sb.append("\"").append(lineContent).append("\"").append(",");
                if (i % 5 == 0) {
                    //批量请求
                    AlipayUserAccountDeviceInfoQueryRequest request = new AlipayUserAccountDeviceInfoQueryRequest();
                    request.setBizContent(String.format("{\"device_type\":\"IMEI\",\"device_ids\":[%s],\"encrypt_type\":\"MD5\",\"request_from\":\"sigmob\"}", sb.deleteLastChar().toString()));

                    BufferedWriter finalBw = bw;
                    AlipayUtil.sendRequest(alipayClient, request, 0, response -> {
                        List<DeviceResultInfo> infos = response.getDeviceInfos();
                        if (infos != null) {
                            for (DeviceResultInfo info : infos) {
                                //System.out.println(String.format("device_id:%s;device_label:%s", info.getDeviceId(), info.getDeviceLabel()));
                                String label = info.getDeviceLabel();
                                //写文件
                                finalBw.write(info.getDeviceId() + ";" + label);
                                finalBw.newLine();
                                finalBw.flush();
                                writtenSize.addAndGet(label.length());
                            }
                        }
                    });

                    sb.deleteAll();
                }

                i++;
            }

            //批量请求
            AlipayUserAccountDeviceInfoQueryRequest request = new AlipayUserAccountDeviceInfoQueryRequest();
            request.setBizContent(String.format("{\"device_type\":\"IMEI\",\"device_ids\":[%s],\"encrypt_type\":\"MD5\",\"request_from\":\"sigmob\"}", sb.deleteLastChar().toString()));
            BufferedWriter finalBw = bw;
            AlipayUtil.sendRequest(alipayClient, request, 0, response -> {
                List<DeviceResultInfo> infos = response.getDeviceInfos();
                if (infos != null) {
                    for (DeviceResultInfo info : infos) {
                        //System.out.println(String.format("device_id:%s;device_label:%s", info.getDeviceId(), info.getDeviceLabel()));
                        String label = info.getDeviceLabel();
                        //写文件
                        finalBw.write(info.getDeviceId() + ";" + label);
                        finalBw.newLine();
                        finalBw.flush();
                        writtenSize.addAndGet(label.length());
                    }
                }
            });

            System.out.println("写入一个子文件，文件名为：" + this.fileName + ", 文件大小为：" + this.fileSize);
        } catch (IOException e) {
            //TODO 日志记录
            System.err.println("写文件错误！");
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.err.println("关闭文件流错误！");
                    e.printStackTrace();
                }
            }
            //利于GC
            this.fileContent = null;
        }
        return writtenSize.get();
    }


    public String getFileDir() {
        return fileDir;
    }


    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }


    public String getFileName() {
        return fileName;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public List<String> getFileContent() {
        return fileContent;
    }


    public void setFileContent(List<String> fileContent) {
        this.fileContent = fileContent;
    }


    public int getFileSize() {
        return fileSize;
    }


    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

}
