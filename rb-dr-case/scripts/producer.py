from confluent_kafka import Producer
import time

p = Producer({'bootstrap.servers': 'broker-1:9092,broker-2:9093'}, retries=999999)

def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

for data in range(10000):
    for part in range(1,8):
        # Trigger any available delivery report callbacks from previous produce() calls
        p.poll(0)

        p.produce('sequence', partition=part,key=str(1),value=str(data), callback=delivery_report)
    p.flush()

    while 1==1:
	    with open("control.info") as f: 
			flag = f.read() 
			if ("true" in flag):
    			time.sleep(1)
    			break
    		else:
    			print "producer stopped!"
