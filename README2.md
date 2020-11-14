```bash
mvn clean assembly:assembly

java -classpath target\filespilt-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.daoqidlv.filespilt.Test C:\Users\yushu\Downloads\file_split zhifubao_request_20201113.txt 1 FORKJOIN

python src/main/python/combine_files.py
```

```sql
add jar /data/shuanghe.yu/sigmob-bigdata-processing.jar;
CREATE TEMPORARY FUNCTION ifnull AS 'com.happyelements.hive.udf.checknull.CheckNullUDF';
CREATE TEMPORARY FUNCTION alipay_user_query AS 'com.sigmob.hive.udf.dmp.rta.AlipayUserQueryUDF';

SET hive.execution.engine=mr;

CREATE TABLE IF NOT EXISTS tmp.zhifubao_request_20201113 AS 
SELECT
a.uid,md5(a.uid) as imei_md5
FROM (
    SELECT
    uid
    ,full_name_split
    FROM (
        SELECT
        ifnull(imei) as uid
        ,full_name
        FROM raw_table.android_applist
        where ds='2020-11-12' and full_name is not null and ifnull(imei) is not null
    ) t
    lateral view outer explode(full_name) tmp as full_name_split
    where full_name_split like '%支付宝%'
) a
join (
    SELECT
    ifnull(imei) as uid
    ,os as equip_os
    ,case when ifnull(idfa) is not null then 'idfa'
          when ifnull(google_ad_id) is not null then 'google_aid'
          when ifnull(imei) is not null then 'imei'
          when ifnull(android_id) is not null then 'android_id'
          end as type
    FROM dwd.ad_request_info_d
    where source='3' and ds>date_sub('2020-11-12',7) and ds<='2020-11-12' and os='2'
    and app_id in (select appid from raw_dim.sig_app_inside where ds='2020-11-12' and role='外部')
    and ifnull(imei) is not null
) b
on (a.uid=b.uid)
group by a.uid
;




CREATE TABLE `tmp.zhifubao_result_20201113` (
  `uid` string,
  `imei_md5` string
)
COMMENT '支付宝接口返回结果'
ROW FORMAT SERDE 
  'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' 
WITH SERDEPROPERTIES ( 
  'colelction.delim'='|', 
  'field.delim'=';', 
  'line.delim'='\n', 
  'mapkey.delim'='~', 
  'serialization.format'='\t') 
STORED AS INPUTFORMAT 
  'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
;

LOAD DATA LOCAL INPATH '/home/data_deployer/result.txt' OVERWRITE INTO TABLE tmp.zhifubao_result_20201113;




select
a.uid
from tmp.zhifubao_request_20201113 a
join (select * from tmp.zhifubao_result_20201113 where imei_md5 in ('L00002','L00016','L00009','L00008','L00005')) b
on (a.imei_md5=b.uid)
group by a.uid
;
```
