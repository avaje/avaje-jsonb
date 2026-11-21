package io.avaje.json.stream.core;

import io.avaje.json.stream.JsonOutput;

import java.io.InputStream;

/**
 * Different pool implementations use different strategies on retaining recyclers for reuse. For
 */
final class Recyclers {

  private Recyclers() {}

  static final int GENERATOR_BUFFER_SIZE = intSetting("jsonb.generatorBufferSize", "JSONB_GENERATOR_BUFFER_SIZE", 4096);
  static final int PARSER_BUFFER_SIZE = intSetting("jsonb.parserBufferSize", "JSONB_PARSER_BUFFER_SIZE", 4096);
  static final int PARSER_CHAR_BUFFER_SIZE = intSetting("jsonb.parserCharBufferSize", "JSONB_PARSER_CHAR_BUFFER_SIZE", 4096);
  static final ParserLimits DEFAULT_PARSER_LIMITS = new ParserLimits(
    intSetting("jsonb.parserMaxNumberDigits", "JSONB_PARSER_MAX_NUMBER_DIGITS", 309),
    intSetting("jsonb.parserMaxStringBuffer", "JSONB_PARSER_MAX_STRING_BUFFER", 50_000),
    intSetting("jsonb.parserMaxRawBuffer", "JSONB_PARSER_MAX_RAW_BUFFER", 10_000_000));

  static int intSetting(String sysProp, String envVar, int defaultValue) {
    String val = System.getProperty(sysProp);
    if (val != null) return Integer.parseInt(val);
    val = System.getenv(envVar);
    if (val != null) return Integer.parseInt(val);
    return defaultValue;
  }

  private static JGenerator createGenerator() {
    return new JGenerator(GENERATOR_BUFFER_SIZE);
  }

  static JParser createParser() {
    return createParser(DEFAULT_PARSER_LIMITS);
  }

  static JParser createParser(ParserLimits limits) {
    final char[] ch = new char[PARSER_CHAR_BUFFER_SIZE];
    final byte[] by = new byte[PARSER_BUFFER_SIZE];
    return new JParser(
        ch,
        by,
        0,
        JParser.DoublePrecision.DEFAULT,
        JParser.UnknownNumberParsing.BIGDECIMAL,
        limits.maxNumberDigits(),
        limits.maxStringBuffer(),
        limits.maxRawBuffer());
  }

  /**
   * Default {@link BufferRecycler} implementation that uses {@link ThreadLocal}
   * for recycling instances.
   *
   * <p>Note that this implementation may not work well on platforms where
   * {@link java.lang.Thread}s are not long-living or reused (like Project Loom).
   */
  static final class ThreadLocalPool implements BufferRecycler {

    private final ThreadLocal<JParser> PARSER = ThreadLocal.withInitial(Recyclers::createParser);
    private final ThreadLocal<JGenerator> GENERATOR = ThreadLocal.withInitial(Recyclers::createGenerator);

    private static final BufferRecycler GLOBAL = new ThreadLocalPool();

    static BufferRecycler shared() {
      return GLOBAL;
    }

    private ThreadLocalPool() {}

    @Override
    public JsonGenerator generator(JsonOutput target) {
      return GENERATOR.get().prepare(target);
    }

    @Override
    public JsonParser parser(byte[] bytes, ParserLimits limits) {
      return PARSER.get().process(bytes, bytes.length, limits);
    }

    @Override
    public JsonParser parser(InputStream in, ParserLimits limits) {
      return PARSER.get().process(in, limits);
    }

    @Override
    public void recycle(JsonGenerator recycler) {
      // nothing to do
    }

    @Override
    public void recycle(JsonParser recycler) {
      // nothing to do
    }
  }

  /**
   * {@link BufferRecycler} implementation that does not use any pool but simply creates new
   * instances when necessary.
   */
  static final class NonRecyclingPool implements BufferRecycler {

    private static final BufferRecycler GLOBAL = new NonRecyclingPool();

    private NonRecyclingPool() {}

    static BufferRecycler shared() {
      return GLOBAL;
    }

    @Override
    public JsonGenerator generator(JsonOutput target) {
      return createGenerator().prepare(target);
    }

    @Override
    public JsonParser parser(byte[] bytes, ParserLimits limits) {
      return createParser(limits).process(bytes, bytes.length, limits);
    }

    @Override
    public JsonParser parser(InputStream in, ParserLimits limits) {
      return createParser(limits).process(in, limits);
    }

    @Override
    public void recycle(JsonGenerator recycler) {
      // nothing to do
    }

    @Override
    public void recycle(JsonParser recycler) {
      // nothing to do
    }
  }
}
