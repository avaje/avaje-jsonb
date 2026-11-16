package io.avaje.json.stream.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Initialising the LOCK_FREE pool before the HYBRID_POOL must not leave the hybrid virtual
 * thread recycler null (#595). Uses an isolated class loader so static init runs fresh.
 */
class HybridBufferRecyclerInitTest {

  @Test
  void lockFreeInitialisedFirst_hybridVirtualRecyclerNotNull() throws Exception {
    try (URLClassLoader loader = isolatedLoader()) {
      Class<?> bufferRecycler = loader.loadClass(BufferRecycler.class.getName());
      Method lockFreePool = bufferRecycler.getDeclaredMethod("lockFreePool");
      lockFreePool.setAccessible(true);
      Object lockFree = lockFreePool.invoke(null);
      assertThat(lockFree).isNotNull();

      Class<?> hybrid = loader.loadClass(HybridBufferRecycler.class.getName());
      Field virtualRecycler = hybrid.getDeclaredField("VIRTUAL_RECYCLER");
      virtualRecycler.setAccessible(true);
      assertThat(virtualRecycler.get(null)).isSameAs(lockFree);
    }
  }

  private static URLClassLoader isolatedLoader() throws Exception {
    List<URL> urls = new ArrayList<>();
    for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
      urls.add(new File(entry).toURI().toURL());
    }
    return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
  }
}
