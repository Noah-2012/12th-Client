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

package com.noadsch12.event.events;

import com.noadsch12.event.listeners.AbstractListener;
import com.noadsch12.event.listeners.MouseScrollListener;

import java.util.ArrayList;

public class MouseScrollEvent extends AbstractEvent {
	private final double horizontal;
	private final double vertical;
	private final double mouseX;
	private final double mouseY;

	public MouseScrollEvent(double horizontal, double vertical, double mouseX, double mouseY) {
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public double GetVertical() {
		return vertical;
	}

	public double GetHorizontal() {
		return horizontal;
	}

	public double GetMouseX() {
		return mouseX;
	}

	public double GetMouseY() {
		return mouseY;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			MouseScrollListener mouseScrollListener = (MouseScrollListener) listener;
			mouseScrollListener.onMouseScroll(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<MouseScrollListener> GetListenerClassType() {
		return MouseScrollListener.class;
	}
}
