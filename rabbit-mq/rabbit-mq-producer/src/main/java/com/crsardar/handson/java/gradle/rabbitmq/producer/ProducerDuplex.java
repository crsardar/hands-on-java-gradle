package com.crsardar.handson.java.gradle.rabbitmq.producer;
/**
 * @author Chittaranjan Sardar
 */

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerDuplex {

    private static final String REGISTRATION_REQUEST_QUEUE = "registration_request_queue";
    private static final String REGISTRATION_RESPONSE_QUEUE = "registration_response_queue";

    private static ConcurrentHashMap<String, String> registrationRequestMap = new ConcurrentHashMap<>();

    private static Channel requestChannel;
    private static Channel responseChannel;

    private static final long MOCK_VALUE = 10_000;

    private static final long REG_RESPONSE_TIME = 60_000; // millis

    private static final long MAX_SERVER_CAPACITY = 10_000;

    private static final AtomicInteger serverPressureCount = new AtomicInteger(0);

    public static void main(String[] argv) throws Exception {

        // Populate Test Cases
        for (int i = 0; i < MOCK_VALUE; i++) {

            String regID = "REG_NO_" + i;
            registrationRequestMap.put(regID, "Registration request for " + i + ".");
        }

        initRegistrationProcess();

        startListeningForRegResponses();
    }

    private static void initRegistrationProcess() {
        new Thread(
                () -> {

                    final long startTime = System.currentTimeMillis();

                    while (registrationRequestMap.size() > 0) {

                        Set<Map.Entry<String, String>> pendingRequest = registrationRequestMap.entrySet();

                        System.out.println("======> Still Registration Pending for " + pendingRequest.size());

                        for (Map.Entry<String, String> entry : pendingRequest) {

                            if (serverPressureCount.get() > MAX_SERVER_CAPACITY) {
                                break;
                            }
                            sendRegistrationRequest(entry.getKey());
                        }

                        try {
                            Thread.sleep(REG_RESPONSE_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    final long endTime = System.currentTimeMillis();

                    System.out.println("============> REGISTRATION  COMPLETED FOR " + MOCK_VALUE
                            + " : Time taken (millis) = " + (endTime - startTime));
                }
        ).start();
    }

    private static void sendRegistrationRequest(final Object payLoad) {

        if (serverPressureCount.get() > MAX_SERVER_CAPACITY) {

            return;
        }

        serverPressureCount.incrementAndGet();

        System.out.println("sendRegistrationRequest : payLoad   = " + payLoad + " : serverPressureCount  = " + serverPressureCount.get());

        if (requestChannel == null || (!requestChannel.isOpen())) {

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");

            try {

                Connection connection = connectionFactory.newConnection();

                requestChannel = connection.createChannel();
                requestChannel.queueDeclare(REGISTRATION_REQUEST_QUEUE, true, false, false, null);

            } catch (IOException e) {

                e.printStackTrace();
                return;

            } catch (TimeoutException e) {

                e.printStackTrace();
                return;
            }
        }

        try {

            requestChannel.basicPublish("", REGISTRATION_REQUEST_QUEUE,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    payLoad.toString().getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startListeningForRegResponses() throws Exception {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Connection connection = connectionFactory.newConnection();

        responseChannel = connection.createChannel();
        responseChannel.queueDeclare(REGISTRATION_RESPONSE_QUEUE, true, false, false, null);
        responseChannel.basicQos(1);

        responseChannel.basicConsume(REGISTRATION_RESPONSE_QUEUE, false, ProducerDuplex::processRegResponse, (consumerTag) -> {
        });
    }

    private static void processRegResponse(String consumerTag, Delivery delivery) throws IOException {

        String regID = new String(delivery.getBody(), StandardCharsets.UTF_8);

        System.out.println("processRegResponse : Processing Response : " + regID);

        if(registrationRequestMap.containsKey(regID)) {

            serverPressureCount.decrementAndGet();

            registrationRequestMap.remove(regID);

            System.out.println("processRegResponse : Still Pending = " + registrationRequestMap.size());
        }

        responseChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }
}
