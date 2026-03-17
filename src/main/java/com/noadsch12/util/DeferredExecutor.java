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

package com.noadsch12.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

public class DeferredExecutor {

    private static final List<Task> TASKS = new CopyOnWriteArrayList<>();

    public static void register(BooleanSupplier condition, Runnable operation) {
        if (condition.getAsBoolean()) {
            operation.run();
            return;
        }

        TASKS.add(new Task(condition, operation));
    }

    public static void tick() {
        for (Task task : TASKS) {
            if (task.condition.getAsBoolean()) {
                try {
                    task.operation.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                TASKS.remove(task); // safe for CopyOnWriteArrayList
            }
        }
    }

    private static class Task {
        BooleanSupplier condition;
        Runnable operation;

        Task(BooleanSupplier condition, Runnable operation) {
            this.condition = condition;
            this.operation = operation;
        }
    }
}