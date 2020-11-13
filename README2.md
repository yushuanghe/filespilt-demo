java -classpath target\filespilt-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.daoqidlv.filespilt.Test C:\Users\yushu\Downloads\file_split zhifubao_request_20201113.txt 1 FORKJOIN

```sql
add jar /data/shuanghe.yu/sigmob-bigdata-processing.jar;
CREATE TEMPORARY FUNCTION ifnull AS 'com.happyelements.hive.udf.checknull.CheckNullUDF';
CREATE TEMPORARY FUNCTION alipay_user_query AS 'com.sigmob.hive.udf.dmp.rta.AlipayUserQueryUDF';

SET hive.execution.engine=mr;

CREATE TABLE IF NOT EXISTS tmp.zhifubao_request_20201113 AS 
SELECT
a.uid
FROM (
    SELECT
    uid
    ,full_name_split
    FROM (
        SELECT
        nvl(ifnull(google_aid),nvl(ifnull(imei),ifnull(android_id))) as uid
        ,full_name
        FROM raw_table.android_applist
        where ds='2020-11-12' and full_name is not null and nvl(ifnull(google_aid),nvl(ifnull(imei),ifnull(android_id))) is not null
    ) t
    lateral view outer explode(full_name) tmp as full_name_split
    where full_name_split like '%支付宝%'
) a
join (
    SELECT
    nvl(ifnull(unique_id),ifnull(idfa)) as uid
    ,os as equip_os
    ,case when ifnull(idfa) is not null then 'idfa'
          when ifnull(google_ad_id) is not null then 'google_aid'
          when ifnull(imei) is not null then 'imei'
          when ifnull(android_id) is not null then 'android_id'
          end as type
    FROM dwd.ad_request_info_d
    where source='3' and ds>date_sub('2020-11-12',7) and ds<='2020-11-12' and os='2'
    and app_id in (select appid from raw_dim.sig_app_inside where ds='2020-11-12' and role='外部')
    and nvl(ifnull(unique_id),ifnull(idfa)) is not null
) b
on (a.uid=b.uid)
group by a.uid
;




set mapreduce.input.fileinputformat.split.maxsize=600000;
set mapreduce.map.cpu.vcores=4;
set mapreduce.map.memory.mb=2048;
set mapreduce.map.java.opts=-Xmx1536m -XX:+UseConcMarkSweepGC;
set hive.exec.reducers.bytes.per.reducer=600000;
set mapreduce.reduce.cpu.vcores=4;

set mapred.max.split.size=600000;
set mapred.min.split.size.per.node=600000;
set mapred.min.split.size.per.rack=600000;

CREATE TABLE IF NOT EXISTS tmp.zhifubao_result_20201113 AS 
SELECT
uid,alipay_user_query(md5(uid)) as label
FROM tmp.zhifubao_request_20201113
;
```