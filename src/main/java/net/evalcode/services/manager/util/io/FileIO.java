package net.evalcode.services.manager.util.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
  static final Logger LOG=LoggerFactory.getLogger(FileIO.class);

  static final int READ_BUFFER_SIZE=4096;


  // MEMBERS
  final Charset charset;


  // CONSTRUCTION
  @Inject
  public FileIO(@Named("net.evalcode.services.charset") final Charset charset)
  {
    this.charset=charset;
  }


  // ACCESSORS/MUTATORS
  public String readFile(final File file)
  {
    try(final FileInputStream fileInputStream=new FileInputStream(file))
    {
      return readInputStream(fileInputStream);
    }
    catch(final FileNotFoundException e)
    {
      LOG.error(e.getMessage(), e);
    }
    catch(final IOException e)
    {
      LOG.error(e.getMessage(), e);
    }

    return null;
  }

  public String readResource(final URL url)
  {
    try(final InputStream inputStream=url.openStream())
    {
      return readInputStream(inputStream);
    }
    catch(final IOException e)
    {
      LOG.error(e.getMessage(), e);
    }

    return null;
  }

  public String readInputStream(final InputStream inputStream)
  {
    final StringBuilder stringBuilder=new StringBuilder();

    final byte[] buffer=new byte[READ_BUFFER_SIZE];
    final ByteBuffer byteBuffer=ByteBuffer.allocateDirect(READ_BUFFER_SIZE);

    try
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
      LOG.error(e.getMessage(), e);
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
      LOG.error(e.getMessage(), e);
    }
  }

  public void createFile(final File file) throws IOException
  {
    Files.createDirectories(file.toPath().getParent());
    Files.createFile(file.toPath());
  }
}
