/*
 * Copyright (c) 2017 original authors and authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dsngroup.broke.broker.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.dsngroup.broke.protocol.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscription pool that stores all subscriptions of a server session.
 * */
public class SubscriptionPool {

    /**
     * The subscriber pool
     * The List of subscribers
     * */
    private List<Subscription> subscriptionList;

    /**
     * An incremental packet ID
     * */
    private PacketIdGenerator packetIdGenerator;

    /**
     * Register the subscriber to an interested topic
     * @param topic Interested topic
     * @param groupId consumer group id
     * @param channel subscriber's Channel {@see Channel}
     * @return the subscriber's instance
     * */
    public Subscription register(String topic, MqttQoS qos, int groupId, Channel channel) {

        Subscription subscription = new Subscription(topic, qos, groupId, channel);

        subscriptionList.add(subscription);

        return subscription;
    }

    /**
     * Unregister the subscriber from the interested topic keyed by subscriber's ChannelHandlerContext
     * @param topic topic to unsubscribe
     * @param channel the subscriber's ChannelHandlerContext
     * @Exception no such topic or subscriber
     * */
    public void unRegister(String topic, Channel channel) throws Exception{
        // TODO: How to remove a closed subscriber channel context?
        // TODO: e.g. on channel close, call unRegister(channelHandlerContext);
        // TODO: Currently, message sending to inactive subscribers is
        // TODO: avoided by explicitly checking by channel().isActive() method
        synchronized (subscriptionList) {
            for (int i = 0; i < subscriptionList.size(); i++) {
                Subscription subscription = subscriptionList.get(i);
                if (subscription.getTopic().equals(topic)) {
                    subscriptionList.remove(i);
                }
            }
        }
    }

    /**
     * For a PUBLISH message, check whether any subscription in the subscription pool matches its topic.
     * If the topic is matched, create a PUBLISH and publish to the corresponding subscriber client.
     * @param mqttPublishMessageIn The PUBLISH message from the publisher.
     * */
    public void sendToSubscribers(MqttPublishMessage mqttPublishMessageIn) {

        String topic = mqttPublishMessageIn.variableHeader().topicName();

        for(Subscription subscription: subscriptionList) {
            // TODO: implement a match method that performs pattern matching between publish topic and subscribe topic filter
            if (subscription.getTopic().equals(topic)) {
                int packetId = packetIdGenerator.getPacketId();
                // TODO: perform QoS selection between publish QoS and subscription QoS
                MqttFixedHeader mqttFixedHeader =
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, subscription.getQos(), false, 0);
                MqttPublishVariableHeader mqttPublishVariableHeader
                        = new MqttPublishVariableHeader(subscription.getTopic(), packetId);
                // TODO: figure out how to avoid being garbage collected.
                ByteBuf payload = Unpooled.copiedBuffer(mqttPublishMessageIn.payload());
                // TODO: figure out how to avoid being garbage collected.
                payload.retain();
                MqttPublishMessage mqttPublishMessageOut
                        = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, payload);
                if(subscription.getSubscriberChannel().isActive()) {
                    try {
                        // TODO: try to remove sync()
                        subscription.getSubscriberChannel().writeAndFlush(mqttPublishMessageOut).sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Constructor
     * */
    public SubscriptionPool() {
        subscriptionList = new LinkedList<>();
        packetIdGenerator = new PacketIdGenerator();

    }

}

/**
 * The comparator for comparing ChannelHandlerContext in HashMap
 * TODO: remove this comparator because hashmap doesn't need this.
 * TODO: but observer how does the hashmap deals channel handler context as the key.
 * */
class ChannelHandlerComparator implements Comparator<ChannelHandlerContext> {
    @Override
    public int compare(ChannelHandlerContext o1, ChannelHandlerContext o2) {
        if (o1 == o2)
            return 0;
        else
            return o1.hashCode()-o2.hashCode();
    }
}

/**
 * Packet Id should between 1~65535
 * */
class PacketIdGenerator {

    private AtomicInteger packetId;

    int getPacketId() {
        int retVal = packetId.getAndIncrement();
        if(retVal > 65535) {
            synchronized (this) {
                if(packetId.get() > 65535) {
                    packetId.set(1);
                    retVal = packetId.getAndIncrement();
                }
            }
        }
        return retVal;

    }

    PacketIdGenerator() {
        packetId = new AtomicInteger();
        packetId.set(1);
    }

}