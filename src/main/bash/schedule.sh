#!/bin/bash

set -e
set -x

day1=$(date -d "-1 day" +%F)
today_ds=$(date +"%Y%m%d")

base_dir='/data/shuanghe.yu/dmp/rta/zhifubao/'
file_split_dir="${base_dir}file_split/"

## 请求数据
sql="
add jar /data/shuanghe.yu/sigmob-bigdata-processing.jar;
CREATE TEMPORARY FUNCTION ifnull AS 'com.happyelements.hive.udf.checknull.CheckNullUDF';

SET hive.execution.engine=mr;

CREATE TABLE IF NOT EXISTS tmp.zhifubao_request_${today_ds} AS
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
        where ds='${day1}' and full_name is not null and ifnull(imei) is not null
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
    where source='3' and ds>date_sub('${day1}',7) and ds<='${day1}' and os='2'
    and app_id in (select appid from raw_dim.sig_app_inside where ds='${day1}' and role='外部')
    and ifnull(imei) is not null
) b
on (a.uid=b.uid)
group by a.uid
;
"
hive -e "${sql}"

rm -rf "${file_split_dir}"
mkdir -p "${file_split_dir}"
hive -e "select imei_md5 from tmp.zhifubao_request_${today_ds}" > "${file_split_dir}zhifubao_request_${today_ds}.txt"
## 请求接口
java -classpath "${base_dir}filespilt-0.0.1-SNAPSHOT-jar-with-dependencies.jar" com.daoqidlv.filespilt.Test "${file_split_dir}" "zhifubao_request_${today_ds}.txt" 1 FORKJOIN > "${base_dir}log.log"

## 合并返回结果
PYTHONIOENCODING=utf-8 /usr/lib/anaconda3/bin/python "${base_dir}combine_files.py" "${file_split_dir}" "zhifubao_request_${today_ds}.txt"

## load 返回结果
sql="
CREATE TABLE tmp.zhifubao_result_${today_ds} (
  imei_md5 string,
  label string
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

LOAD DATA LOCAL INPATH '/home/data_deployer/shuanghe.yu/dmp/rta/zhifubao/file_split/result.txt' OVERWRITE INTO TABLE tmp.zhifubao_result_${today_ds};
"
hive -e "${sql}"

## 导出包
sql="
select
a.uid
from tmp.zhifubao_request_${today_ds} a
join (select * from tmp.zhifubao_result_${today_ds} where label in ('L00016','L00009','L00008','L00005')) b
on (a.imei_md5=b.imei_md5)
group by a.uid
;
"
hive -e "${sql}" > "${base_dir}zhifubao_result_${today_ds}.txt"
tar -zcf "${base_dir}zhifubao_result_${today_ds}.tar.gz" "${base_dir}zhifubao_result_${today_ds}.txt"

set +x
