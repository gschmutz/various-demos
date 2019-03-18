import json
import requests

from subprocess import check_output

from confluent_kafka import Consumer, KafkaError

# Get your API_KEY from your settings file ('~/.tipboard/settings-local.py').
API_KEY = 'api-key-here'
# Change '127.0.0.1:7272' to the address of your Tipboard instance.
API_URL = 'http://localhost:80/api/v0.1/{}'.format(API_KEY)
API_URL_PUSH = '/'.join((API_URL, 'push'))
API_URL_TILECONFIG = '/'.join((API_URL, 'tileconfig'))

def prepare_for_just_value(data):
    # Listing needs data as a list of lists (whose elements are pairs
    # component-percentage), so we have to prepare it.
    # data={"title": "Number of Tweets:", "description": "(1 hour)", "just-value": "23"
    data_prepared = data
    data_prepared = {'title': '# Tweets:', 'description': 'per hour', 'just-value': data_prepared}
    return data_prepared


def main():
    # Tile 'pie001' (pie chart)
    # (let's say we want to show issues count for project 'Tipboard' grouped by
    # issue status i.e. 'Resolved', 'In Progress', 'Open', 'Closed' etc.)
    TILE_NAME = 'just_value'
    TILE_KEY = 'nof_tweets'

    c = Consumer({
       'bootstrap.servers': '192.168.73.86:9092',
       'group.id': 'test-consumer-group',
       'default.topic.config': {
           'auto.offset.reset': 'largest'
       }
    })

    c.subscribe(['DASH_TWEET_COUNT_BY_HOUR_T'])

    while True:
       msg = c.poll(1.0)

       if msg is None:
          continue
       if msg.error():
          if msg.error().code() == KafkaError._PARTITION_EOF:
             continue
          else:
             print(msg.error())
             break

       data = json.loads(msg.value().decode('utf-8'))
       data_selected = data.get('NOF_TWEETS')
       # print (data_selected)
       data_prepared = prepare_for_just_value(data_selected)
       data_jsoned = json.dumps(data_prepared)
       data_to_push = {
           'tile': TILE_NAME,
           'key': TILE_KEY,
           'data': data_jsoned,
       }
       resp = requests.post(API_URL_PUSH, data=data_to_push)
       if resp.status_code != 200:
          print(resp.text)
          return


if __name__ == '__main__':
    main()