/*
 * Copyright 2016 - 2018 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.events;

import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.traccar.BaseEventHandler;
import org.traccar.Context;
import org.traccar.model.Event;
import org.traccar.model.Position;

@ChannelHandler.Sharable
public class AlertEventHandler extends BaseEventHandler {

    private final boolean ignoreDuplicateAlerts;

    public AlertEventHandler() {
        ignoreDuplicateAlerts = Context.getConfig().getBoolean("event.ignoreDuplicateAlerts");
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Object alarms = position.getAttributes().get(Position.KEY_ALARM);
        if (alarms != null) {
            Map<Event, Position> eventMap = new LinkedHashMap<>();
            String[] alarmList = alarms.toString().split(",");
            for (String alarm : alarmList) {
                boolean ignoreAlert = false;
                if (ignoreDuplicateAlerts) {
                    Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
                    if (lastPosition != null) {
                        Object alarmAttrs = lastPosition.getAttributes().get(Position.KEY_ALARM);
                        if (alarmAttrs != null && alarmAttrs.toString().contains(alarm)) {
                            ignoreAlert = true;
                        }
                    }
                }

                if (!ignoreAlert) {
                    Event event = new Event(Event.TYPE_ALARM, position.getDeviceId(), position.getId());
                    event.set(Position.KEY_ALARM, (String) alarm);
                    eventMap.put(event, position);
                }
            }
            return eventMap;
        }
        return null;
    }

}
