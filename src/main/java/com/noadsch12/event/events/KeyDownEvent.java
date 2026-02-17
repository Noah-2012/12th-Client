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

/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package com.noadsch12.event.events;

import com.noadsch12.event.listeners.AbstractListener;
import com.noadsch12.event.listeners.KeyDownListener;

import java.util.ArrayList;

public class KeyDownEvent extends AbstractEvent {
	private final long window;
	private final int key;
	private final int scancode;
	private final int action;
	private final int modifiers;

	public KeyDownEvent(long window, int key, int scancode, int action, int modifiers) {
        this.window = window;
		this.key = key;
		this.scancode = scancode;
		this.action = action;
		this.modifiers = modifiers;
	}

	public long GetWindow() {
		return window;
	}

	public int GetKey() {
		return key;
	}

	public int GetScanCode() {
		return scancode;
	}

	public int GetAction() {
		return action;
	}

	public int GetModifiers() {
		return modifiers;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
	    ArrayList<AbstractListener> listenersCopy = new ArrayList<>(listeners);
	    for (AbstractListener listener : listenersCopy) {
	        KeyDownListener keyDownListener = (KeyDownListener) listener;
	        keyDownListener.onKeyDown(this);
	    }
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<KeyDownListener> GetListenerClassType() {
		return KeyDownListener.class;
	}
}