package io.avaje.jsonb.generator.models.valid;

import java.util.Set;

public class ExamplePacket extends Packet {

    private final Set<String> ids;

    public ExamplePacket (final Set<String> ids) {
        this.ids = ids;
    }

    public Set<String> getIds() {
        return this.ids;
    }

}