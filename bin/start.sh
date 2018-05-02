#!/usr/bin/env bash
nohup \
spark-submit \
--master yarn \
--deploy-mode cluster \
--name 11874_TopicDemo_1507865725449 \
--class demo.TopicDemo \
--executor-cores 10 \
--driver-memory 5g \
--executor-memory 6g \
--num-executors 50 \
/home/mart_cd/data_dir/wm_datacenter/streaming/topic_demo/spark-streaming-1.0-SNAPSHOT.jar \
abc \
>./TopicDemo_1507865725449.log 2>&1 \
&