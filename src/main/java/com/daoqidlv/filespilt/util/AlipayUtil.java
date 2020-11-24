package com.daoqidlv.filespilt.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayUserAccountDeviceInfoQueryRequest;
import com.alipay.api.response.AlipayUserAccountDeviceInfoQueryResponse;

/**
 * Description:
 * Date: 2020-11-24
 * Time: 16:30
 *
 * @author yushu
 */
public class AlipayUtil {
    public static final String APPID = "2019102468624246";
    public static final String PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCPzdRu1Eis1/oANtTYM5sUcUPRQZ+8estM/LGtnuRORyyoK9md8gP5ELX4Hlx4TAsocUALdQmeUvQDrkZc0EOuJShsxAJOYywIxIaKBXCVRBIAufQIFoxsiKnsMDrGVAvEwfnK08tln3TGBRuosAJdTnsMTwjKYDz3gXyFD/FTcXsjnq6+n3gwltR5VvW843b4XQY5cvEj2hDQKXgjMH8iZnqrbj1eWguqBnVX8/nJUXDZeokVhAlTo914cm7QLsnwn6lbP0dAtuJ8ThmpKQD45eJD7SQyijq8htQXC3HinjtSweouga67C7fMF2YMUYOpNpD5LmjfrcO2IqIMA+txAgMBAAECggEASt4WYz0SOSqYddQWLBlx+8qcTC2mRDKJ9aL5vjKjGj3j+goaWijN+LXCh1MHQXoVtRCBD3X3c/4sHF53M4saMWgXC8lgif8HxejLQsBiRNQTq10kt7FFmpvsG5NghjDrj5yNuBNcKaRC33V5WPGJyZo6ZcYgBXzlQNahRYVvVslOOpvtRHE7ShwSuAZ+qw46/hsQIR7thbGwxlEEz2LlCNAGpNDqvuxZ1gra15gElKnDSY7whuWKh5Cg8z0t3MWsOXjiYhehZtJ56QY1dOhdP45NqDuR3gyuiz9ZCP2IiIPV3pYmUYSoHsMuUBvmVE83jxctXvp8W3eTgxX3O+C0IQKBgQDFt+wZWfddNBlucSTwgWUKanocy32kC91Cnam3SDJmyLDopuE5ONHN25R4gZR7oF0N1MTZ0wWcJo9Ti9QoJ2znSCVS0lSFfKrZKJ0ZmGjM87Oj7WdjkZCE3wHNvnWnw+psyPC4eh/zg6Y+8+LM8GNMIbqwPuqezzkJXTVq1nsknwKBgQC6MXVrrJN1mLxTsbFHqq43pBwRQPKcOt33A13/KB/5po+zHUinHIkTeNEO4tYgunbCEhQGnC7x2YD/TRcI8MJMTkjKLXx3tV9IkLv+9i0/59FyVErai/plbzZcqD85awT4egA3OrEhSmYnfPQGSkl7yeEamqIuYPpZB4YT9iZl7wKBgARfGahB1fdIlJtdgwySP9KoIgUK9r8ux/iWFc+ImByvqUGrluU1/WWIylKTwt2s4QHSR3vtb31RWO/m8ozkukKu1KxtHBNLe1eLz1VmikKlzL3+HMAV0tUtPRP4dw97hG5dTv01LgmrSnyv1b7kPb3MLR2CdpuGR2OOnQS8s0JPAoGAa1txNPCA5eF3VVqzuXr0kSiS9m/Yc7SNn4vVevYL2crsOH6+EX0mnI+hG1A33w8kwqnh2h3cA59B/fLZQVJw9cZ9ufWYOv3dPt4VR+FyHdLwtVddAb6MVkhVs/9e1SCb8RmHXlD9KRDY7hPiMG5NCI9SuCBJOyy3rPXlnUTkxwkCgYByWmcAZt2LbW/e43X6HGVqhlbd9ngImEjgogVCpCuFJRxHDFOXWJ+KarcVqTQvmZjI94KxNTZVhMvLqJlUT7O6qV2Orypi3aznXFJDDrj50Aom9N6uCs9reoGuOPbaJNIIPbYofYjgQsUn/SWXhwAFeSxp6ZyYUjBr7zofy6FrjA==";
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgykYYFPQy9JSxxcZPdQGucg5Oon50Li+6JpSTRVDajUHStp1ibcWcb7dUDLCnHNjgBkBo6syJdVmcQfiS8LXD0mVFjtnreu3ijH2iQN/wUDR7MRJSl08/KYiv6j3GZ//jtgULWPpd6c/ZtT8mu9++ZWaFu0EsQA98aci7So7EXmV1lrv9/ERUMdW7uSBzxNpiXrNx1P0PYrv/6Vnpxzre3KDbeJvqmAN5kzXBHBZzzYtyEZ/XkBoRNO4mkdTsdy5XIokB132EgY+ezpw19/qF9yz218/gwVYpRDp6f514eBauCIT7zCrcuA4FGvp0Vjo4KV151jGr+GsOpCsCD2XtQIDAQAB";
    public static final String METHOD = "https://ugapi.alipay.com/gateway.do";

    private static final int DEFAULT_RETRY_COUNT = 2;

    /**
     * @param alipayClient
     * @param request
     * @param callback
     * @param retryCount   重试次数
     */
    public static void sendRequest(AlipayClient alipayClient, AlipayUserAccountDeviceInfoQueryRequest request, int retryCount, RequestCallback callback) {
        if (retryCount >= DEFAULT_RETRY_COUNT) {
            //重试两次，放弃
            System.out.println("放弃");
            return;
        }

        AlipayUserAccountDeviceInfoQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (response.isSuccess()) {
                //System.out.println("调用成功");
                callback.process(response);
            } else {
                //System.out.println("调用失败");
                //重试
                System.out.printf("第%s次重试", ++retryCount);
                sendRequest(alipayClient, request, retryCount, callback);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            //重试
            System.out.printf("第%s次重试", ++retryCount);
            sendRequest(alipayClient, request, retryCount, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 静态内部类：请求回调接口
     *
     * @author Shuanghe Yu
     */
    public static interface RequestCallback {

        /**
         * 处理查询结果
         *
         * @param response 支付宝rta返回
         * @throws Exception
         */
        void process(AlipayUserAccountDeviceInfoQueryResponse response) throws Exception;

    }
}
