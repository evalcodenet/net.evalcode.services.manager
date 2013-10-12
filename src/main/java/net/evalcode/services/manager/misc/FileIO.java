package net.evalcode.services.manager.misc;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FileIO
 *
 * @author carsten.schipke@gmail.com
 */
public final class FileIO
{
  // PREDEFINED PROPERTIES
  private static final int READ_BUFFER_SIZE=4096;
  private static final Logger LOG=LoggerFactory.getLogger(FileIO.class);


  // MEMBERS
  private final Charset charset;


  // CONSTRUCTION
  @Inject
  public FileIO(@Named("net.evalcode.services.charset") final Charset charset)
  {
    this.charset=charset;
  }


  // ACCESSORS/MUTATORS
  public String readFile(final File file)
  {
    final StringBuilder stringBuilder=new StringBuilder();

    final byte[] buffer=new byte[READ_BUFFER_SIZE];
    final ByteBuffer byteBuffer=ByteBuffer.allocateDirect(READ_BUFFER_SIZE);

    try(final FileInputStream inputStream=new FileInputStream(file))
    {
      int read=0;

      while(0<=(read=inputStream.read(buffer)))
      {
        byteBuffer.put(buffer, 0, read);
        byteBuffer.flip();

        stringBuilder.append(charset.decode(byteBuffer));

        byteBuffer.compact();
      }
    }
    catch(final IOException e)
    {
      LOG.warn(e.getMessage(), e);
    }

    return stringBuilder.toString();
  }

  public void writeFile(final File file, final String content, final boolean createFile)
    throws IOException
  {
    if(createFile && !file.exists())
      createFile(file);

    final ByteBuffer byteBuffer=charset.encode(content);

    try(final FileOutputStream outputStream=new FileOutputStream(file))
    {
      outputStream.write(byteBuffer.array(), 0, byteBuffer.limit());
    }
    catch(final IOException e)
    {
      LOG.warn(e.getMessage(), e);
    }
  }

  public void createFile(final File file) throws IOException
  {
    final File path=file.getParentFile();

    if(!path.exists())
      path.mkdirs();

    file.createNewFile();
  }
}
