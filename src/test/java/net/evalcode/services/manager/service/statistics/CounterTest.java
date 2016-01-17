package net.evalcode.services.manager.service.statistics;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.evalcode.services.manager.service.statistics.Counter;
import org.junit.Test;


/**
 * Test {@link Counter}
 *
 * @author carsten.schipke@gmail.com
 */
public class CounterTest
{
  // TESTS
  @Test
  public void testIncrement()
  {
    assertEquals(0, Counter.getAll().size());

    Counter.increment(CounterTest.class.getName());

    assertEquals(1, Counter.getAll().size());
    assertTrue(Counter.getKeys().contains(CounterTest.class.getName()));

    assertEquals(1L, Counter.get(CounterTest.class.getName()));
  }
}
