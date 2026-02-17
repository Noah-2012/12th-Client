/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.event;

import com.noadsch12.event.events.AbstractEvent;
import com.noadsch12.event.listeners.AbstractListener;

import java.util.ArrayList;

public class EventBus {

    private static final ArrayList<AbstractListener> listeners = new ArrayList<>();

    public static void register(AbstractListener listener) {
        listeners.add(listener);
    }

    public static void unregister(AbstractListener listener) {
        listeners.remove(listener);
    }

    public static void post(AbstractEvent event) {
        event.Fire(listeners);
    }
}
