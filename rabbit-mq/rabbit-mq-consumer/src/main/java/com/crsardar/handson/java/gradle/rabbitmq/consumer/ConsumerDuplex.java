package com.crsardar.handson.java.gradle.rabbitmq.consumer;
/**
 * @author Chittaranjan Sardar
 */

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerDuplex {

    private static final String REGISTRATION_REQUEST_QUEUE = "registration_request_queue";
    private static final String REGISTRATION_RESPONSE_QUEUE = "registration_response_queue";

    private static ConcurrentHashMap<String, String> registrationResponseMap = new ConcurrentHashMap<>();

    private static Channel processingChannel;
    private static Channel responseChannel;

    private static Long startTime = null;

    private static final long REG_PROCESSING_TIME = 500; // millis

    public static void main(String[] argv) throws Exception {

        initResponseChannel();

        startListeningForRegRequest();
    }

    private static void initResponseChannel() {

        if (responseChannel == null || (!responseChannel.isOpen())) {

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");

            try {

                Connection connection = connectionFactory.newConnection();

                responseChannel = connection.createChannel();
                responseChannel.queueDeclare(REGISTRATION_RESPONSE_QUEUE, true, false, false, null);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private static void startListeningForRegRequest() throws Exception {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Connection connection = connectionFactory.newConnection();

        processingChannel = connection.createChannel();
        processingChannel.queueDeclare(REGISTRATION_REQUEST_QUEUE, true, false, false, null);
        processingChannel.basicQos(1);

        processingChannel.basicConsume(REGISTRATION_REQUEST_QUEUE, false, ConsumerDuplex::processRegRequest, (consumerTag) -> {
        });
    }

    private static void processRegRequest(String consumerTag, Delivery delivery) throws IOException {

        String regID = new String(delivery.getBody(), StandardCharsets.UTF_8);

        if(startTime == null)
        {
            startTime = System.currentTimeMillis();
        }

        System.out.println("processRegRequest : Processing Request : " + regID);

        if (registrationResponseMap.containsKey(regID)) {

            System.out.println("processRegRequest : regID = " + regID
                    + " : Already Processed at " + registrationResponseMap.get(regID));
        } else {

            registrationResponseMap.put(regID, "Time = " + System.currentTimeMillis());

            try {

                Thread.sleep(REG_PROCESSING_TIME);

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            responseChannel.basicPublish("", REGISTRATION_RESPONSE_QUEUE,
                    MessageProperties.PERSISTENT_TEXT_PLAIN, regID.getBytes(StandardCharsets.UTF_8));

            System.out.println("processRegRequest : Processed : regID = " + regID
                    + " : total processed = " + registrationResponseMap.size() + " : in " + (System.currentTimeMillis() - startTime) + " millis.");
        }

        processingChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }
}
