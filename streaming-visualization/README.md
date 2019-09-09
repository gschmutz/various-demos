# Streaming Visualization Demo

This demo shows various solutions for implementing streaming visualization applications using Kafka Connect / KSQL as the stream data integration and stream analytics stack. 

## Running on AWS Lightstail

```
# Install Docker 
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable edge"
apt-get install -y docker-ce
sudo usermod -a -G docker $USER

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Install wget
apt-get install -y wget

# Install kafkacat
apt-get install -y kafkacat

# Prepare Environment
export PUBLIC_IP=$(curl ipinfo.io/ip)
export DOCKER_HOST_IP=$(ip addr show eth0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)
mkdir analyticsplatform
cd analyticsplatform
wget https://raw.githubusercontent.com/gschmutz/various-demos/master/streaming-visualization/docker/docker-compose.yml

# Setup Kafka Connect Twitter Connector
mkdir kafka-connect
cd kafka-connect
wget https://github.com/jcustenborder/kafka-connect-twitter/releases/download/0.2.26/kafka-connect-twitter-0.2.26.tar.gz
mkdir kafka-connect-twitter-0.2.26
tar -zxvf kafka-connect-twitter-0.2.26.tar.gz -C kafka-connect-twitter-0.2.26
rm kafka-connect-jms-1.2.1-2.1.0-all.tar.gz

# Startup Environment
docker-compose up
```


## Prepare

### Create topics

Create the topic `tweet-raw-v1` and `tweet-term-v1`

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic tweet-raw-v1 --replication-factor 3 --partitions 8
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic tweet-term-v1 --replication-factor 3 --partitions 8
```

### Setup Kafka Connector for Twitter Source

Get the Kafka Connector from here: <https://github.com/jcustenborder/kafka-connect-twitter>

```
cd kafka-connect
mkdir kafka-connect-twitter-0.2.26
cd kafka-connect-twitter-0.2.26
```

```
wget https://github.com/jcustenborder/kafka-connect-twitter/releases/download/0.2.26/kafka-connect-twitter-0.2.26.tar.gz
```

```
tar -xvzf kafka-connect-twitter-0.2.26.tar.gz
rm kafka-connect-twitter-0.2.26.tar.gz
```

Create the connector 

```
connector.class=com.github.jcustenborder.kafka.connect.twitter.TwitterSourceConnector
process.deletes=false
filter.keywords=#vdz19 
kafka.status.topic=tweet-raw-v1
tasks.max=1
twitter.oauth.consumerKey=wd6ohwZCiS4qI4woGqPnNhEd4
twitter.oauth.consumerSecret=XXXXXX
twitter.oauth.accessToken=18898576-2Qzx1PlhCL2ZkCBVZvX0epzKOSoOaZ9ABaeL7ndd5
twitter.oauth.accessTokenSecret=XXXXXX


# do not use transform currently
#transforms.createKey.type=org.apache.kafka.connect.transforms.ValueToKey
#transforms=createKey,extractInt
#transforms.extractInt.type=org.apache.kafka.connect.transforms.ExtractField$Key
#transforms.extractInt.field=Id
#transforms.createKey.fields=Id
```


```
{
  "connector.class": "com.github.jcustenborder.kafka.connect.twitter.TwitterSourceConnector",
  "tasks.max": "1",
  "twitter.oauth.consumerKey": "wd6ohwZCiS4qI4woGqPnNhEd4",
  "twitter.oauth.consumerSecret": "3OUIaM4VmzDLyldB377lawzmupebgQqp7Bb5PrAPVLVUI28PRs",
  "twitter.oauth.accessToken": "18898576-2Qzx1PlhCL2ZkCBVZvX0epzKOSoOaZ9ABaeL7ndd5",
  "twitter.oauth.accessTokenSecret": "ZzAOSAVDXoojMqcViZ7q9TmOFvqPzx1WjoN0Wvd5tPYZD",
  "process.deletes": "false",
  "filter.keywords": "trump",
  "kafka.status.topic": "tweet-raw-v2",
  "transforms.createKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
  "transforms": "createKey,extractInt",
  "transforms.createKey.fields": "Id",
  "transforms.extractInt.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
  "transforms.extractInt.field": "Id"
}
```



## KSQL Stream Processing

First let's connect to the KSQL CLI

```
docker run --rm -it --network analyticsplatform_default confluentinc/cp-ksql-cli:5.1.2 http://ksql-server-1:8088
```

next we will create the following streams and tables

* `tweet_raw_s` - the raw tweets in a hierarchical format
* `tweet_s` - the id, text and screenName of all the tweets
* `tweet_with_geo_s` - the raw tweets in a hierarchical format
* `tweet_term_s` - all the terms for all the tweets (hashtag and word)
* `tweet_terms_per_min_t` - a table with the terms per minute
* `tweet_terms_per_hour_t` - a table with the terms per hour
* `tweet_count_by_min_t` - the number of tweets by minute
* `tweet_count_by_hour_t` - the number of tweets by hour

### Register Raw Topic Stream

First we create a new stream `tweet_raw_s` which holds all the raw tweets:

```
CREATE STREAM tweet_raw_s WITH (KAFKA_TOPIC='tweet-raw-v1', VALUE_FORMAT='AVRO');
```

### Tweets

```
CREATE STREAM tweet_s WITH (KAFKA_TOPIC='tweet-v1', VALUE_FORMAT='AVRO', PARTITIONS=8)
AS SELECT id
,	createdAt
,	text
,	user->screenName
FROM tweet_raw_s;
```

### Tweets with Geo Location

```
CREATE STREAM tweet_with_geo_s WITH (KAFKA_TOPIC='tweet-with-geo-v1', VALUE_FORMAT='AVRO', PARTITIONS=8)
AS SELECT id
,	text
,	user->screenName
,	geolocation->latitude
,	geolocation->longitude
FROM tweet_raw_s
WHERE geolocation->latitude is not null;
```

### Create the Term Stream

Register the Avro Schema for terms as subject `tweet-term-v1-value`

```
{
  "type": "record",
  "name": "TweetTerms",
  "namespace": "com.trivadis.twitter.sample",
  "fields": [
    {
      "name": "id",
      "type": [
        "null",
        {
          "type": "long",
          "connect.doc": "Returns the id of the status"
        }
      ],
      "doc": "Returns the id of the status",
      "default": null
    },
    {
      "name": "lang",
      "type": [
        "null",
        {
          "type": "string",
          "connect.doc": "The language as returned in the Tweet"
        }
      ],
      "doc": "The language as returned in the Tweet",
      "default": null
    },    
    {
      "name": "term",
      "type": [
        "null",
        {
          "type": "string",
          "connect.doc": "Returns the term found in the tweet"
        }
      ],
      "doc": "Returns the id of the status",
      "default": null
    },    
    {
      "name": "type",
      "type": [
        "null",
        {
          "type": "string",
          "connect.doc": "Returns the type of the term (hashtag, url, word, username)"
        }
      ],
      "doc": "Returns the id of the status",
      "default": null
    }    
  ],
  "connect.doc": "Twitter Terms"
}
```

Now create the empty stream. We will then using multiple inserts statements to publish to the stream.

```
DROP STREAM tweet_term_s;

CREATE STREAM tweet_term_s \
WITH (kafka_topic='tweet-term-v1', \
value_format='AVRO');
```

#### Populate Hashtags

Hashtags are organized as an array. Currently there is no way in KSQL to dynamically read over the arrays, all you can do is access it by index. 

```
SELECT id, LCASE(hashtagentities[0]->text) from tweet_raw_s where hashtagentities[0] IS NOT NULL;
```

The code below currently handles a max of 6 hashtags per Tweet:
        
```
INSERT INTO tweet_term_s \
SELECT id, lang, TRIM(LCASE(hashtagentities[0]->text)) as term, 'hashtag' as type from tweet_raw_s where hashtagentities[0] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, TRIM(LCASE(hashtagentities[1]->text)) as term, 'hashtag' as type from tweet_raw_s where hashtagentities[1] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, TRIM(LCASE(hashtagentities[2]->text)) as term, 'hashtag' as type from tweet_raw_s where hashtagentities[2] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, TRIM(LCASE(hashtagentities[3]->text)) as term, 'hashtag' as type from tweet_raw_s where hashtagentities[3] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, TRIM(LCASE(hashtagentities[4]->text)) as term, 'hashtag' as type from tweet_raw_s where hashtagentities[4] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, TRIM(LCASE(hashtagentities[5]->text)) as term, 'hashtag' as type from tweet_raw_s where hashtagentities[5] IS NOT NULL;
```


```
select type, collect_set (term) from tweet_term_s window tumbling (size 30 seconds) group by type;
```

```
select type, histogram (term) from tweet_term_s window tumbling (size 30 seconds) group by type;
```

#### Populate Words

```
CREATE STREAM tweet_words_s WITH (kafka_topic='tweet-words-v1', value_format='AVRO', PARTITIONS=8)
AS SELECT id, lang, removestopwords(split(LCASE(text), ' ')) AS word FROM tweet_raw_s WHERE lang = 'en' or lang = 'de';
```

```
INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[0]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[0] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[1]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[1] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[2]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[2] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[3]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[3] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[4]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[4] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[5]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[5] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[6]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[6] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[7]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[7] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[8]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[8] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[9]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[9] IS NOT NULL;

INSERT INTO tweet_term_s \
SELECT id, lang, replacestring(replacestring(replacestring(replacestring(TRIM(word[10]),'#',''),'@',''),'.',''),':','') as term, 'word' as type from tweet_words_s where word[10] IS NOT NULL;
```

### Terms per 1 minute

```
DROP TABLE tweet_terms_per_min_t;

CREATE TABLE tweet_terms_per_min_t AS
SELECT windowstart() windowStart, windowend() windowEnd, type, term, count(*) terms_per_min FROM tweet_term_s window TUMBLING (SIZE 60 seconds) where lang = 'en' or lang = 'de' GROUP by type, term;
```

```
SELECT TIMESTAMPTOSTRING(windowStart, 'yyyy-MM-dd HH:mm:ss.SSS'), TIMESTAMPTOSTRING(windowEnd, 'yyyy-MM-dd HH:mm:ss.SSS'), tweets_per_min, term FROM tweet_terms_per_min_t WHERE type = 'hashtag';
```

### Terms per 1 hour

```
DROP TABLE tweet_terms_per_hour_t;

CREATE TABLE tweet_terms_per_hour_t AS
SELECT windowstart() windowStart, windowend() windowEnd, type, term, count(*) terms_per_hour FROM tweet_term_s window TUMBLING (SIZE 60 minutes) where lang = 'en' or lang = 'de' GROUP by type, term;
```

### Top 10 Terms per hour (this does not work!)


```
DROP STREAM tweet_terms_per_hour_s;

CREATE STREAM tweet_terms_per_hour_s WITH (KAFKA_TOPIC='TWEET_TERMS_PER_HOUR_T', VALUE_FORMAT='AVRO');
```

```
DROP TABLE tweet_hashtag_top_1hour_t;

CREATE TABLE tweet_hashtag_top_1hour_t
AS SELECT type, windowstart, topkdistinct (CONCAT(LEFTPAD(CAST(terms_per_hour AS VARCHAR),5,'0'), term),10) top_10, topkdistinct (terms_per_hour,20) top_20 from tweet_terms_per_hour_s group by type, windowstart;
```



### Tweets Total

First we create a stream with an "artifical" group id so that we can count on "one single group" later, as KSQL does not allow an aggregate operation without a group by operation. 

```
DROP STREAM tweet_count_s

CREATE STREAM tweet_count_s
AS SELECT 1 AS groupId, id, 
TIMESTAMPTOSTRING(ROWTIME, 'yyyy-MM-dd HH:mm:ss.SSS') AS rowtimefull, SUBSTRING(TIMESTAMPTOSTRING(ROWTIME, 'yyyy-MM-dd HH:mm:ss.SSS'),0,11) as rowtimedate, SUBSTRING(TIMESTAMPTOSTRING(ROWTIME, 'yyyy-MM-dd HH:mm:ss.SSS'),11,6) as rowtimeHHMM
FROM tweet_raw_s;
```

now with this stream we can count by hour

```
DROP TABLE tweet_count_by_hour_t;

CREATE TABLE tweet_count_by_hour_t
AS SELECT groupid, windowstart() windowStart, windowend() windowEnd, COUNT(*) tweets_per_hour FROM tweet_count_s WINDOW TUMBLING (SIZE 1 HOUR) GROUP BY groupid;
```

and count by minute

```
DROP TABLE tweet_count_by_min_t;

CREATE TABLE tweet_count_by_min_t
AS SELECT groupid, windowstart() windowStart, windowend() windowEnd, count(*) tweets_per_min FROM tweet_count_s  
WINDOW TUMBLING (size 60 seconds) GROUP BY groupid; 
```


## Tweets per user

Due to <https://github.com/confluentinc/ksql/pull/2076> this is not possible (should work in 5.2):

```
SELECT user->screenname, count(*) AS tweet_count_by_user FROM tweet_raw_s GROUP BY user->screenname having count(*) > 1;
```

As a workaround first create a stream where the user is unpacked and then used

```
DROP STREAM tweet_tweets_with_user_s;

CREATE STREAM tweet_tweets_with_user_s
AS SELECT id, text, user->screenname as user_screenname, createdat from tweet_raw_s;


CREATE TABLE tweet_by_user_t
AS SELECT user_screenname, COUNT(*) nof_tweets_by_user FROM tweet_tweets_with_user_s GROUP BY user_screenname having count(*) > 1;
```
## Arcadia Data

Navigate to <http://127.0.0.1:7999/arc/apps/login?next=/arc/apps/> and login as user `admin` with password `admin`.

## Tipboard Dashboard

```
docker run --rm -it --network analyticsplatform_default confluentinc/cp-ksql-cli:5.1.2 http://ksql-server-1:8088
```

<http://allegro.tech/tipboard/>
<https://tipboard.readthedocs.io>

```
DROP TABLE dash_hashtag_top10_5min_t;

CREATE TABLE dash_hashtag_top10_5min_t WITH (VALUE_FORMAT = 'JSON')
AS SELECT TIMESTAMPTOSTRING(windowstart(), 'yyyy-MM-dd HH:mm:ss.SSS'), type, term, count(*) nof from tweet_term_s window hopping (size 5 minutes, advance by 1 minute) where lang = 'en' and type = 'hashtag' group by type, term;
```

```
CREATE TABLE dash_tweet_count_t WITH (VALUE_FORMAT = 'JSON')
AS SELECT groupid, COUNT(*) nof_tweets FROM tweet_count_s GROUP BY groupid;
```

```
DROP TABLE dash_tweet_count_by_hour_t;

CREATE TABLE dash_tweet_count_by_hour_t WITH (VALUE_FORMAT = 'JSON')
AS SELECT groupid, COUNT(*) nof_tweets FROM tweet_count_s WINDOW TUMBLING (SIZE 1 HOUR) GROUP BY groupid;
```



```
DROP STREAM dash_tweets_s;

CREATE STREAM dash_tweets_s WITH (VALUE_FORMAT = 'JSON')
AS SELECT id, user->screenName screenName, text FROM tweet_raw_s;
```


```
python consume-json-nof-tweets.py 
```

```
curl -X POST http://localhost:80/api/v0.1/api-key-here/push -d "tile=just_value" -d "key=nof_tweets" -d 'data={"title": "Number of Tweets:", "description": "(1 hour)", "just-value": "23"}'
```

```
curl -X POST http://localhost:80/api/v0.1/api-key-here/push -d "tile=text" -d "key=tweet" -d 'data={"text": "The need for data-driven organizations and cultures isn’t going away. Firms need to take a hard look at why these initiatives are failing to gain business traction: https://t.co/V7iNuoEfB0 via @harvardbiz #BigData #DataDiscovery #DataAnalytics"}'
```

```
curl -X POST http://localhost:80/api/v0.1/api-key-here/push -d "tile=listing" -d "key=top_hashtags" -d 'data={"items": ["bigdata", "machinelearning", "ksql", "kafka"]}'
```


## Slack

```
DROP STREAM slack_notify_s;

CREATE STREAM slack_notify_s WITH (KAFKA_TOPIC='slack-notify', VALUE_FORMAT='AVRO')
AS SELECT id, text, user->screenname as user_screenname, createdat from tweet_raw_s
where user->screenname = 'gschmutz' or user->screenname = 'VoxxedZurich';
```

# Demo

## Slack

```
kafkacat -b analyticsplatform -t tweet-raw-v1 -o end -q
```

```
kafkacat -b analyticsplatform -t slack-notify -o end -q
```

```
Live Demo "Streaming Visualization": Now showing integration with #KafkaConnect and #Slack #bigdata2019 #vdz19
```

## Tipboard

```
Live Demo "Streaming Visualization": Now showing integration with #Kafka and #Tipboard #vdz19
```

## Arcadia Data

```
SELECT * FROM tweet_raw_s;
```

```
DESCRIBE tweet_raw_s;
```

```
SELECT text, user->screenname FROM tweet_raw_s;
```

```
DESCRIBE tweet_term_s;
```

```
SELECT * FROM tweet_term_s;
```

```
SELECT windowstart() windowStart, windowend() windowEnd, type, term, count(*) terms_per_min 
FROM tweet_term_s 
WINDOW TUMBLING (SIZE 60 seconds) 
WHERE lang = 'en' or lang = 'de' 
GROUP by type, term;
```


```
Live Demo "Streaming Visualization": Now showing integration with #Kafka, #KSQL and #ArcadiaData #vdz19
```


