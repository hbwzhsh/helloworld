/*
 * Copyright 1999-2101 Alibaba Group.
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
package rushmore.zbus.common.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

import rushmore.zbus.common.json.parser.DefaultJSONParser;
import rushmore.zbus.common.json.parser.JSONToken;
import rushmore.zbus.common.json.parser.deserializer.ObjectDeserializer;

public class UUIDCodec implements ObjectSerializer, ObjectDeserializer {

    public final static UUIDCodec instance = new UUIDCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }

        UUID uid = (UUID) object;
        serializer.write(uid.toString());
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        
        String name = (String) parser.parse();
        
        if (name == null) {
            return null;
        }
        
        return (T) UUID.fromString(name);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}