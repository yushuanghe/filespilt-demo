package com.daoqidlv.filespilt.single.forkjoin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.DeviceResultInfo;
import com.alipay.api.request.AlipayUserAccountDeviceInfoQueryRequest;
import com.alipay.api.response.AlipayUserAccountDeviceInfoQueryResponse;
import com.daoqidlv.filespilt.Util;
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
        int writtenSize = 0;

        String APPID = "2019102468624246";
        String PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCPzdRu1Eis1/oANtTYM5sUcUPRQZ+8estM/LGtnuRORyyoK9md8gP5ELX4Hlx4TAsocUALdQmeUvQDrkZc0EOuJShsxAJOYywIxIaKBXCVRBIAufQIFoxsiKnsMDrGVAvEwfnK08tln3TGBRuosAJdTnsMTwjKYDz3gXyFD/FTcXsjnq6+n3gwltR5VvW843b4XQY5cvEj2hDQKXgjMH8iZnqrbj1eWguqBnVX8/nJUXDZeokVhAlTo914cm7QLsnwn6lbP0dAtuJ8ThmpKQD45eJD7SQyijq8htQXC3HinjtSweouga67C7fMF2YMUYOpNpD5LmjfrcO2IqIMA+txAgMBAAECggEASt4WYz0SOSqYddQWLBlx+8qcTC2mRDKJ9aL5vjKjGj3j+goaWijN+LXCh1MHQXoVtRCBD3X3c/4sHF53M4saMWgXC8lgif8HxejLQsBiRNQTq10kt7FFmpvsG5NghjDrj5yNuBNcKaRC33V5WPGJyZo6ZcYgBXzlQNahRYVvVslOOpvtRHE7ShwSuAZ+qw46/hsQIR7thbGwxlEEz2LlCNAGpNDqvuxZ1gra15gElKnDSY7whuWKh5Cg8z0t3MWsOXjiYhehZtJ56QY1dOhdP45NqDuR3gyuiz9ZCP2IiIPV3pYmUYSoHsMuUBvmVE83jxctXvp8W3eTgxX3O+C0IQKBgQDFt+wZWfddNBlucSTwgWUKanocy32kC91Cnam3SDJmyLDopuE5ONHN25R4gZR7oF0N1MTZ0wWcJo9Ti9QoJ2znSCVS0lSFfKrZKJ0ZmGjM87Oj7WdjkZCE3wHNvnWnw+psyPC4eh/zg6Y+8+LM8GNMIbqwPuqezzkJXTVq1nsknwKBgQC6MXVrrJN1mLxTsbFHqq43pBwRQPKcOt33A13/KB/5po+zHUinHIkTeNEO4tYgunbCEhQGnC7x2YD/TRcI8MJMTkjKLXx3tV9IkLv+9i0/59FyVErai/plbzZcqD85awT4egA3OrEhSmYnfPQGSkl7yeEamqIuYPpZB4YT9iZl7wKBgARfGahB1fdIlJtdgwySP9KoIgUK9r8ux/iWFc+ImByvqUGrluU1/WWIylKTwt2s4QHSR3vtb31RWO/m8ozkukKu1KxtHBNLe1eLz1VmikKlzL3+HMAV0tUtPRP4dw97hG5dTv01LgmrSnyv1b7kPb3MLR2CdpuGR2OOnQS8s0JPAoGAa1txNPCA5eF3VVqzuXr0kSiS9m/Yc7SNn4vVevYL2crsOH6+EX0mnI+hG1A33w8kwqnh2h3cA59B/fLZQVJw9cZ9ufWYOv3dPt4VR+FyHdLwtVddAb6MVkhVs/9e1SCb8RmHXlD9KRDY7hPiMG5NCI9SuCBJOyy3rPXlnUTkxwkCgYByWmcAZt2LbW/e43X6HGVqhlbd9ngImEjgogVCpCuFJRxHDFOXWJ+KarcVqTQvmZjI94KxNTZVhMvLqJlUT7O6qV2Orypi3aznXFJDDrj50Aom9N6uCs9reoGuOPbaJNIIPbYofYjgQsUn/SWXhwAFeSxp6ZyYUjBr7zofy6FrjA==";
        String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgykYYFPQy9JSxxcZPdQGucg5Oon50Li+6JpSTRVDajUHStp1ibcWcb7dUDLCnHNjgBkBo6syJdVmcQfiS8LXD0mVFjtnreu3ijH2iQN/wUDR7MRJSl08/KYiv6j3GZ//jtgULWPpd6c/ZtT8mu9++ZWaFu0EsQA98aci7So7EXmV1lrv9/ERUMdW7uSBzxNpiXrNx1P0PYrv/6Vnpxzre3KDbeJvqmAN5kzXBHBZzzYtyEZ/XkBoRNO4mkdTsdy5XIokB132EgY+ezpw19/qF9yz218/gwVYpRDp6f514eBauCIT7zCrcuA4FGvp0Vjo4KV151jGr+GsOpCsCD2XtQIDAQAB";
        String METHOD = "https://ugapi.alipay.com/gateway.do";

        AlipayClient ALIPAY_CLIENT = new DefaultAlipayClient(METHOD, APPID, PRIVATE_KEY, "json", "utf-8", PUBLIC_KEY, "RSA2");

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
                    AlipayUserAccountDeviceInfoQueryResponse response = null;
                    try {
                        response = ALIPAY_CLIENT.execute(request);
                        if (response.isSuccess()) {
                            //System.out.println("调用成功");
                            List<DeviceResultInfo> infos = response.getDeviceInfos();
                            if (infos!=null){
                                for (DeviceResultInfo info : infos) {
                                    //System.out.println(String.format("device_id:%s;device_label:%s", info.getDeviceId(), info.getDeviceLabel()));
                                    String label = info.getDeviceLabel();
                                    //写文件
                                    bw.write(info.getDeviceId() + ";" + label);
                                    bw.newLine();
                                    bw.flush();
                                    writtenSize += label.length();
                                }
                            }
                            
                        } else {
                            //System.out.println("调用失败");
                        }
                    } catch (AlipayApiException e) {
                        e.printStackTrace();
                    }
                    sb.deleteAll();
                }

                i++;
            }

            //批量请求
            AlipayUserAccountDeviceInfoQueryRequest request = new AlipayUserAccountDeviceInfoQueryRequest();
            request.setBizContent(String.format("{\"device_type\":\"IMEI\",\"device_ids\":[%s],\"encrypt_type\":\"MD5\",\"request_from\":\"sigmob\"}", sb.deleteLastChar().toString()));
            AlipayUserAccountDeviceInfoQueryResponse response = null;
            try {
                response = ALIPAY_CLIENT.execute(request);
                if (response.isSuccess()) {
                    //System.out.println("调用成功");
                    List<DeviceResultInfo> infos = response.getDeviceInfos();
                    if (infos!=null){
                        for (DeviceResultInfo info : infos) {
                            //System.out.println(String.format("device_id:%s;device_label:%s", info.getDeviceId(), info.getDeviceLabel()));
                            String label = info.getDeviceLabel();
                            //写文件
                            bw.write(info.getDeviceId() + ";" + label);
                            bw.newLine();
                            bw.flush();
                            writtenSize += label.length();
                        }
                    }
                    
                } else {
                    //System.out.println("调用失败");
                }
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }

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
        return writtenSize;
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
