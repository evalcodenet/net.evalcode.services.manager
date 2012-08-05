package net.evalcode.services.manager.io;


import static org.junit.Assert.assertEquals;
import java.io.File;
import java.nio.charset.Charset;
import net.evalcode.services.manager.misc.FileIO;
import org.junit.Test;


/**
 * Test {@link FileIO}
 *
 * @author carsten.schipke@gmail.com
 */
public class FileIOTest
{
  // TESTS
  @Test
  public void testReadWriteFile() throws Exception
  {
    final Charset charset=Charset.defaultCharset();

    final File file=new File(
      System.getProperty("java.io.tmpdir", "/tmp")+File.separator+"fileIoTest.tmp"
    );

    file.deleteOnExit();

    final String content="foo\nbar\n \n\n";
    final FileIO fileIo=new FileIO(charset);

    fileIo.writeFile(file, content, true);
    final String read=fileIo.readFile(file);

    assertEquals(content, read);

    // make sure we read exactly the bytes we wrote
    final int length=content.getBytes().length;
    final byte[] bytes0=content.getBytes();
    final byte[] bytes1=read.getBytes();

    for(int i=0; i<length; i++)
      assertEquals(bytes0[i], bytes1[i]);
  }
}
