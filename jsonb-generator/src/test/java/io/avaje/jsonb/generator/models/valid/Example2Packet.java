package io.avaje.jsonb.generator.models.valid;


public class Example2Packet extends Packet {

    private final String ids;

    public Example2Packet (final String ids) {
        this.ids = ids;
    }

    public String getIds() {
        return this.ids;
    }
}