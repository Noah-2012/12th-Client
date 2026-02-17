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
import com.noadsch12.event.listeners.SendMessageListener;

import java.util.ArrayList;

public class SendMessageEvent extends AbstractEvent {
	private String message;

	public SendMessageEvent(String message) {
		this.message = message;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			SendMessageListener sendMessageListener = (SendMessageListener) listener;
			sendMessageListener.onSendMessage(this);
		}
	}

	@Override
	public Class<SendMessageListener> GetListenerClassType() {
		return SendMessageListener.class;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
