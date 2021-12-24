package io.avaje.jsonb.diesel;

import java.io.IOException;

interface UnknownSerializer {
  void serialize(JGenerator writer, Object unknown) throws IOException;
}
