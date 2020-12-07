/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.storage.impl.ksql.serde;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.apicurio.registry.storage.impl.ksql.keys.MessageKey;
import io.apicurio.registry.storage.impl.ksql.keys.MessageTypeToKeyClass;

/**
 * Kafka deserializer responsible for deserializing the key of a KSQL Kafka message.
 * @author eric.wittmann@gmail.com
 */
public class KafkaSqlKeyDeserializer implements Deserializer<MessageKey> {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    }

    /**
     * @see org.apache.kafka.common.serialization.Deserializer#deserialize(java.lang.String, byte[])
     */
    @Override
    public MessageKey deserialize(String topic, byte[] data) {
        try {
            byte msgTypeOrdinal = data[0];
            Class<? extends MessageKey> keyClass = MessageTypeToKeyClass.ordToKeyClass(msgTypeOrdinal);
            UnsynchronizedByteArrayInputStream in = new UnsynchronizedByteArrayInputStream(data, 1);
            MessageKey key = mapper.readValue(in, keyClass);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
