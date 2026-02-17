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

package com.noadsch12;

import java.io.*;
import java.time.LocalDateTime;

public class MACROReport {

    public final String message;
    public final String stacktrace;
    public final LocalDateTime time;

    MACROReport(String message, String stacktrace) {
        this.message = message;
        this.stacktrace = stacktrace;
        this.time = LocalDateTime.now();
    }

    public static MACROReport fromThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));

        return new MACROReport(
                t.toString(),
                sw.toString()
        );
    }

    public void save() {
        try {
            File dir = new File("12th-client/crash-reports");
            dir.mkdirs();

            File file = new File(dir, "crash-" + System.currentTimeMillis() + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(stacktrace);
            }
        } catch (Exception ignored) {}
    }
}
