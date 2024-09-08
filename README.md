# test-kafka-cluster

## Table of Contents
- [Overview](#overview)
- [Components](#components)
    - [Kafka Cluster](#kafka-cluster)
    - [Monitoring Tools](#monitoring-tools)
    - [Application Testing](#application-testing)
- [Use Cases](#use-cases)
- [How to Use](#how-to-use)
- [Starting the Application](#starting-the-application)
- [Application Swagger UI](#application-swagger-ui)
- [Kafka UI](#kafka-ui)
- [Monitoring](#monitoring)
    - [Prometheus](#prometheus)
    - [Alert Manager](#alert-manager)
- [Toxiproxy](#toxiproxy)
    - [List Proxies](#list-proxies)
    - [List Specific Proxy](#list-specific-proxy)
    - [Adding Toxics](#adding-toxics)
        - [Simulate Latency](#simulate-latency)
        - [Simulate Kafka Node Unreachable](#simulate-kafka-node-unreachable-by-the-client)
    - [Removing Toxics](#removing-toxics)
- [Kafka CLI](#kafka-cli)
    - [Install Kafka CLI](#install-kafka-cli)
    - [Describe Topics](#describe-topics)
    - [Describe Consumer Group](#describe-consumer-group)
    - [Reading Kafka Saved Data](#reading-kafka-saved-data)
- [References](#references)

## Overview

The `test-kafka-cluster` project is a development and testing environment for Apache Kafka, designed to simulate and analyze the behavior of Kafka clusters in various conditions. It uses Docker to set up a multi-node Kafka cluster, along with associated tools for monitoring, management, and testing.

## Components

### Kafka Cluster

- **Kafka Nodes**: Three Kafka broker nodes (`kafka-node-1`, `kafka-node-2`, and `kafka-node-3`) are set up in a Docker environment. These nodes are configured for replication and high availability.
- **Toxiproxy**: A tool for simulating network and system failures by introducing faults like latency or disconnections into the Kafka brokers. This helps in testing the resilience and fault tolerance of Kafka applications.
- **Kafka UI**: Provides a web interface for managing and monitoring Kafka topics and consumer groups.

### Monitoring Tools

- **Prometheus**: A monitoring and alerting toolkit used to collect metrics from Kafka and other services. It provides a web interface for querying and visualizing these metrics.
- **Alertmanager**: Handles alerts sent by Prometheus, allowing for notification through various channels if certain conditions are met.

### Application Testing

- **test-app**: A sample application built to interact with the Kafka cluster. It can be used to produce and consume messages, allowing developers to test Kafka's functionality and performance under different conditions.

## Use Cases

1. **Development and Testing**:
    - Provides a local environment for developers to test Kafka-related applications and configurations.
    - Simulates different failure scenarios using Toxiproxy to ensure that Kafka applications can handle various network issues and outages.

2. **Monitoring and Troubleshooting**:
    - Offers insights into the health and performance of Kafka brokers through Prometheus metrics.
    - Allows for real-time monitoring and alerting to quickly address issues or bottlenecks in the Kafka cluster.

3. **Educational Purposes**:
    - Serves as a learning tool for understanding Kafka’s architecture, configuration, and operations.
    - Demonstrates how to set up and manage a Kafka cluster in a controlled environment.

## How to Use

1. **Start the Environment**: Use the provided `docker compose` commands to set up and start the Kafka cluster and associated tools.
2. **Interact with Kafka**: Use Kafka CLI tools and the Kafka UI to manage topics, consumer groups, and examine the cluster's state.
3. **Simulate Failures**: Leverage Toxiproxy to test how your Kafka applications respond to simulated network failures or latency.
4. **Monitor and Alert**: Use Prometheus and Alertmanager to track metrics and get notified of any issues within the Kafka cluster.


## Starting the application

```shell
mvn clean verify && \
  docker compose -p kafka-cluster down && \
  docker compose -p kafka-cluster build --no-cache test-app &&\
  docker compose -p kafka-cluster up -d
```

## Application Swagger UI

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)

## Kafka UI

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

#### Simulate latency

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

#### Simulate Kafka node unreachable by the client

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

## References

- [Shopify/toxiproxy](https://github.com/Shopify/toxiproxy?tab=readme-ov-file#down)
- [Apache Kafka](https://kafka.apache.org/downloads)
