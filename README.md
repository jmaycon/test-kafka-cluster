# test-kafka-cluster

## Table of Contents
- [To start the application](#to-start-the-application)
- [Application](#application)
- [Access Kafka UI](#access-kafka-ui)
- [Monitoring](#monitoring)
    - [Prometheus](#prometheus)
    - [Alert Manager](#alert-manager)
- [Toxiproxy](#toxiproxy)
    - [List Proxies](#list-proxies)
    - [List specific proxy](#list-specific-proxy)
    - [Adding Toxics](#adding-toxics)
        - [Add a latency of 2 minutes to node 1](#add-a-latency-of-2-minutes-to-node-1)        
        - [Simulate bring a service down](#simulate-bring-a-service-down)        
- [Kafka CLI](#kafka-cli)
    - [Install Kafka CLI](#install-kafka-cli)
    - [Describe Topics](#describe-topics)
    - [Describe Consumer Group](#describe-consumer-group)
    - [Reading Kafka saved data](#reading-kafka-saved-data)

## To start the application

```shell
mvn clean verify && \
  docker compose -p kafka-cluster down && \
  docker compose -p kafka-cluster build --no-cache test-app &&\
  docker compose -p kafka-cluster up -d
```

## Application

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)

## Access Kafka UI

- [Kafka UI](http://localhost:28080)

## Monitoring

### Prometheus

- [Prometheus](http://localhost:9090/)

### Alert Manager

- [Alert Manager](http://localhost:9093/alerts)

## Toxiproxy

### List Proxies

```shell
curl http://localhost:8474/proxies
```

### List specific proxy

```shell
curl localhost:8474/proxies/kafka-node-1
```

### Adding Toxics

#### Add a latency of 2 minutes to node 1

- Adding

```shell
KAFKA_NODE=kafka-node-3
curl -X POST http://localhost:8474/proxies/$KAFKA_NODE/toxics \
  -H "Content-Type: application/json" \
  -d '{
    "name": "latency-toxic-upstream",
    "type": "latency",
    "stream": "upstream",
    "attributes": {
      "latency": 120000
    }
  }'
```

- Removing

```shell
curl -X DELETE http://localhost:8474/proxies/$KAFKA_NODE/toxics/latency-toxic-upstream
```

#### Simulate bring a service down

This will only disable the proxy, and simulate the service down. This is done by POSTing to `/proxies/{proxy}` and setting the `enabled` field to `false`.

```shell
KAFKA_NODE=kafka-node-1
curl -X POST http://localhost:8474/proxies/$KAFKA_NODE \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": false
  }'
```

- Enable again

```shell
curl -X POST http://localhost:8474/proxies/$KAFKA_NODE \
-H "Content-Type: application/json" \
-d '{
"enabled": true
}'
```

## Kafka CLI

### Install Kafka CLI

```shell
KAFKA_VERSION=3.7.1
SCALA_VERSION=2.13
KAFKA_PKG_VERSION=kafka_$SCALA_VERSION-$KAFKA_VERSION
rm -rf ./.kafka-cli && mkdir -p ./.kafka-cli/tmp

# Download Kafka to the target folder
wget -P ./.kafka-cli/tmp https://downloads.apache.org/kafka/$KAFKA_VERSION/$KAFKA_PKG_VERSION.tgz

# Extract the downloaded Kafka
tar -xzf ./.kafka-cli/tmp/$KAFKA_PKG_VERSION.tgz -C ./.kafka-cli
rm -rf ./.kafka-cli/tmp

# Move the bin directory to ./kafka-cli
chmod +x ./.kafka-cli/$KAFKA_PKG_VERSION/bin/*

# Export binaries from the current path
export PATH=$PATH:$(pwd)/.kafka-cli/$KAFKA_PKG_VERSION/bin/

# Confirm it's working
kafka-topics.sh  --version
```

### Describe Topics

```shell
TOPIC_NAME=happy-topic
BOOTSTRAP_SERVER=localhost:19092,localhost:29092,localhost:39092
kafka-topics.sh --describe --topic $TOPIC_NAME --bootstrap-server $BOOTSTRAP_SERVER
```

Sample output

```text
Topic: happy-topic      TopicId: t8olDNsjRw2NWH3KqUrQAQ PartitionCount: 3       ReplicationFactor: 1    Configs: min.insync.replicas=2
        Topic: happy-topic      Partition: 0    Leader: 2       Replicas: 2     Isr: 2
        Topic: happy-topic      Partition: 1    Leader: 3       Replicas: 3     Isr: 3
        Topic: happy-topic      Partition: 2    Leader: 1       Replicas: 1     Isr: 1
```

### Describe Consumer Group

```shell
GROUP_ID=test-app
BOOTSTRAP_SERVER=localhost:19092,localhost:29092,localhost:39092
kafka-consumer-groups.sh --describe --group $GROUP_ID --bootstrap-server $BOOTSTRAP_SERVER
```

Sample output

```text
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                              HOST            CLIENT-ID
test-app        happy-topic     1          -               0               -               consumer-test-app-2-498a43c4-5d5e-4e31-906e-421c8d5067a5 /172.18.0.6     consumer-test-app-2
test-app        happy-topic     2          -               0               -               consumer-test-app-3-f100a6db-21ca-487f-8446-5e95ec6afbb3 /172.18.0.6     consumer-test-app-3
test-app        happy-topic     0          -               0               -               consumer-test-app-1-01a82b04-319e-4c7d-a711-78992fbde665 /172.18.0.6     consumer-test-app-1
```

### Reading Kafka saved data

```shell
kafka-run-class.sh kafka.tools.DumpLogSegments \
  --files .kafka-docker-volume/kafka-node-1/happy-topic-1/00000000000000000000.log \
  --print-data-log
```

Sample Output

```text
Dumping .kafka-docker-volume\kafka-node-1\happy-topic-1\00000000000000000000.log
Log starting offset: 0
baseOffset: 0 lastOffset: 0 count: 1 baseSequence: 0 lastSequence: 0 producerId: 2000 producerEpoch: 0 partitionLeaderEpoch: 2 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 0 CreateTime: 1725667102683 size: 77 magic: 2 compresscodec: none crc: 234816192 isvalid: true
| offset: 0 CreateTime: 1725667102683 keySize: 1 valueSize: 8 sequence: 0 headerKeys: [] key: 0 payload: "string"
baseOffset: 1 lastOffset: 1 count: 1 baseSequence: 0 lastSequence: 0 producerId: 4000 producerEpoch: 0 partitionLeaderEpoch: 10 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 77 CreateTime: 1725670160137 size: 77 magic: 2 compresscodec: none crc: 3335080710 isvalid: true
| offset: 1 CreateTime: 1725670160137 keySize: 1 valueSize: 8 sequence: 0 headerKeys: [] key: 0 payload: "string"
```