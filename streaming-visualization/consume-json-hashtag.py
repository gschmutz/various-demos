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

def prepare_for_pie_chart(data):
    # Pie chart needs data as a list of lists (whose elements are pairs
    # component-percentage), so we have to prepare it.
    # data={"title": "My title", "pie_data": [["Pie 1", 25], ["Pie 2", 25], ["Pie 3", 50]]}'
    data_prepared = []
    for k, v in data.items():
        data_prepared.append([k, v[0]])
    data_prepared = {'title': 'my title', 'pie_data': data_prepared}
    return data_prepared

def prepare_for_listing(data):
    # Listing needs data as a list of lists (whose elements are pairs
    # component-percentage), so we have to prepare it.
    # "data={"items": ["Leader: 5", "Product Owner: 0", "Scrum Master: 3", "Developer: 0"]}"
    data_prepared = []
    for k in data:
        data_prepared.append(k)
    data_prepared = {'items': data_prepared}
    print (data_prepared)
    return data_prepared


def main():
    # Tile 'pie001' (pie chart)
    # (let's say we want to show issues count for project 'Tipboard' grouped by
    # issue status i.e. 'Resolved', 'In Progress', 'Open', 'Closed' etc.)
    TILE_NAME = 'listing'
    TILE_KEY = 'top_hashtags'

    c = Consumer({
       'bootstrap.servers': '192.168.73.86:9092',
       'group.id': 'test-consumer-group',
       'default.topic.config': {
           'auto.offset.reset': 'largest'
       }
    })

    c.subscribe(['DASH_HASHTAG_TOP10_5MIN_T'])

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
       data_selected = data.get('TOP_10')
       # print (data_selected)
       data_prepared = prepare_for_listing(data_selected)
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
