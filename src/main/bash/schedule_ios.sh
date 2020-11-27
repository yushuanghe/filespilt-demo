#!/bin/bash

set -e
set -x

day1=$(date -d "-1 day" +%F)
today_ds=$(date +"%Y%m%d")

base_dir='/data/shuanghe.yu/dmp/rta/zhifubao/'
file_split_dir="${base_dir}file_split_ios/"

## 请求数据
sql="
add jar /data/shuanghe.yu/sigmob-bigdata-processing.jar;
CREATE TEMPORARY FUNCTION ifnull AS 'com.happyelements.hive.udf.checknull.CheckNullUDF';

SET hive.execution.engine=mr;

drop table if exists tmp.zhifubao_request_ios_${today_ds};

CREATE TABLE IF NOT EXISTS tmp.zhifubao_request_ios_${today_ds} AS
SELECT
a.uid,md5(a.uid) as uid_md5
FROM (
    SELECT
    ifnull(idfa) as uid
    ,os as equip_os
    ,case when ifnull(idfa) is not null then 'idfa'
          when ifnull(google_ad_id) is not null then 'google_aid'
          when ifnull(imei) is not null then 'imei'
          when ifnull(android_id) is not null then 'android_id'
          end as type
    FROM dwd.ad_request_info_d
    where source='3' and ds>date_sub('${day1}',7) and ds<='${day1}' and os='1'
    and app_id in (select appid from raw_dim.sig_app_inside where ds='${day1}' and role='外部')
    and ifnull(idfa) is not null
) a
group by a.uid
;
"
hive -e "${sql}"

rm -rf "${file_split_dir}"
mkdir -p "${file_split_dir}"
hive -e "select uid_md5 from tmp.zhifubao_request_ios_${today_ds}" > "${file_split_dir}zhifubao_request_ios_${today_ds}.txt"
## 请求接口
java -classpath "${base_dir}filespilt-0.0.1-SNAPSHOT-jar-with-dependencies.jar" com.daoqidlv.filespilt.Test "${file_split_dir}" "zhifubao_request_ios_${today_ds}.txt" 1 FORKJOIN 24 16 10240 1024 IDFA > "${base_dir}log.log"

## 合并返回结果
PYTHONIOENCODING=utf-8 /usr/lib/anaconda3/bin/python "${base_dir}combine_files.py" "${file_split_dir}" "zhifubao_request_ios_${today_ds}.txt"

## load 返回结果
sql="
drop table if exists tmp.zhifubao_result_ios_${today_ds};

CREATE TABLE IF NOT EXISTS tmp.zhifubao_result_ios_${today_ds} (
  uid_md5 string,
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

LOAD DATA LOCAL INPATH '${file_split_dir}result.txt' OVERWRITE INTO TABLE tmp.zhifubao_result_ios_${today_ds};
"
hive -e "${sql}"

## 导出包
sql="
select
a.uid
from tmp.zhifubao_request_ios_${today_ds} a
join (select * from tmp.zhifubao_result_ios_${today_ds} where label in ('L00016','L00009','L00008','L00005')) b
on (a.uid_md5=b.uid_md5)
group by a.uid
;
"
hive -e "${sql}" > "${base_dir}zhifubao_result_ios_${today_ds}.txt"
cd "${base_dir}"
tar -zcf "zhifubao_result_ios_${today_ds}.tar.gz" "zhifubao_result_ios_${today_ds}.txt"

set +x
