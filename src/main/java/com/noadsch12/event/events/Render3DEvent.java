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

import java.util.ArrayList;

import com.noadsch12.event.listeners.AbstractListener;
import com.noadsch12.event.listeners.Render3DListener;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends AbstractEvent {
	MatrixStack matrices;
	Frustum frustum;
	RenderTickCounter renderTickCounter;
	Camera camera;

	public MatrixStack GetMatrix() {
		return matrices;
	}

	public RenderTickCounter getRenderTickCounter() {
		return renderTickCounter;
	}

	public Frustum getFrustum() {
		return frustum;
	}

	public Camera getCamera() {
		return camera;
	}

	public Render3DEvent(MatrixStack matrix4f, Frustum frustum, Camera camera, RenderTickCounter renderTickCounter) {
		matrices = matrix4f;
		this.renderTickCounter = renderTickCounter;
		this.frustum = frustum;
		this.camera = camera;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			Render3DListener renderListener = (Render3DListener) listener;
			renderListener.onRender(this);
		}
	}

	@Override
	public Class<Render3DListener> GetListenerClassType() {
		return Render3DListener.class;
	}
}