/*
 * Copyright 2016 Anton Tananaev (anton@traccar.org)
 * Copyright 2016 Andrey Kunitsyn (andrey@traccar.org)
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

import java.util.Collection;
import java.util.Collections;

import org.traccar.BaseEventHandler;
import org.traccar.Context;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;

public class MaintenanceEventHandler extends BaseEventHandler {

    public static final String ATTRIBUTE_MAINTENANCE_START = "maintenance.start";
    public static final String ATTRIBUTE_MAINTENANCE_INTERVAL = "maintenance.interval";
    public static final String ATTRIBUTE_MAINTENANCE_ATTRIBUTE = "maintenance.attribute";

    @Override
    protected Collection<Event> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getDeviceById(position.getDeviceId());
        if (device == null || !Context.getIdentityManager().isLatestPosition(position)) {
            return null;
        }

        double maintenanceInterval = Context.getDeviceManager()
                .lookupAttributeDouble(device.getId(), ATTRIBUTE_MAINTENANCE_INTERVAL, 0, false);
        if (maintenanceInterval == 0) {
            return null;
        }
        double maintenanceStart = Context.getDeviceManager()
                .lookupAttributeDouble(device.getId(), ATTRIBUTE_MAINTENANCE_START, 0, false);

        String maintenanceAttribute = Context.getDeviceManager()
                .lookupAttributeString(device.getId(), ATTRIBUTE_MAINTENANCE_ATTRIBUTE,
                        Position.KEY_TOTAL_DISTANCE, false);

        double oldTotalAttribute = 0.0;
        double newTotalAttribute = 0.0;

        Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        if (lastPosition != null) {
            oldTotalAttribute = lastPosition.getDouble(maintenanceAttribute);
        }
        newTotalAttribute = position.getDouble(maintenanceAttribute);

        oldTotalAttribute -= maintenanceStart;
        newTotalAttribute -= maintenanceStart;

        if ((long) (oldTotalAttribute / maintenanceInterval) < (long) (newTotalAttribute / maintenanceInterval)) {
            Event event = new Event(Event.TYPE_MAINTENANCE, position.getDeviceId(), position.getId());
            event.set(maintenanceAttribute, newTotalAttribute);
            return Collections.singleton(event);
        }

        return null;
    }

}
