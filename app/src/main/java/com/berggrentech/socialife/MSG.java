package com.berggrentech.socialife;

import android.util.JsonWriter;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

public final class MSG {

    static String register(String name, String group) throws IOException {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.beginObject()
                    .name("type").value("register")
                    .name("group").value(group)
                    .name("member").value(name)
                    .endObject();
            return stringWriter.toString();
    }

    static String unRegister(String id) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject()
                .name("type").value("unregister")
                .name("id").value(id)
                .endObject();
        return stringWriter.toString();
    }

    static String getGroup(String group) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject()
                .name("type").value("members")
                .name("group").value(group)
                .endObject();
        return stringWriter.toString();
    }

    static String getGroups() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject()
                .name("type").value("groups")
                .endObject();
        return stringWriter.toString();
    }

    static String updatePosition(String id, LatLng pos) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject()
                .name("type").value("location")
                .name("id").value(id)
                .name("longitude").value(pos.longitude)
                .name("latitude").value(pos.latitude)
                .endObject();
        return stringWriter.toString();
    }
}
