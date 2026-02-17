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
import com.noadsch12.event.listeners.MouseClickListener;

import java.util.ArrayList;

public class MouseClickEvent extends AbstractEvent {

	public final double mouseX;
	public final double mouseY;
	public final int button;
	public final int action;
	public final int buttonNumber;
	public final int mods;

	public MouseClickEvent(double mouseX, double mouseY, int button, int action, int mods) {
        this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.button = button;
		this.action = action;
		buttonNumber = -1;
		this.mods = mods;
	}

	public MouseClickEvent(double mouseX, double mouseY, int button, int action, int mods, int buttonNumber) {
        this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.button = button;
		this.action = action;
		this.mods = mods;
		this.buttonNumber = buttonNumber;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			MouseClickListener mouseClickListener = (MouseClickListener) listener;
			mouseClickListener.onMouseClick(this);

			if (isCancelled)
				break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<MouseClickListener> GetListenerClassType() {
		return MouseClickListener.class;
	}
}