#!/bin/bash

set -e
set -x

# 利用支付宝接口找出作弊媒体

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
uid,md5(uid) as imei_md5
FROM (
    SELECT
    imei as uid
    FROM tmp.jw_wb_media_1124_02
    union all
    SELECT
    uid
    FROM (
        SELECT
        imei as uid
        FROM tmp.jw_nb_media_1124_02
        limit 200000
    ) t
) t
group by uid
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
cd "${base_dir}"
tar -zcf "zhifubao_result_${today_ds}.tar.gz" "zhifubao_result_${today_ds}.txt"

set +x
