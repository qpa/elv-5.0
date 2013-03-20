package elv.server.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming methods.
 */
public final class Streams {
  /** Streaming buffer size. */
  public static final int BUFFER_SIZE =  8192;
  /** Maximum lines for HTML stream. */
  public static final int MAX_HTML_LINES = 500;
  /** The file encoding in the system.*/
  public static final String ENCODING = "UTF-8";
  /** The "CSV" separator character. */
  public static final char CSV_SEPARATOR = ';';
  /** The "CSV" separator string. */
  public static String CSV_SEP = "" + CSV_SEPARATOR;
  
  /**
   * Streams from binary to binary with default buffer size and closes the streams.
   * @param inputStream the readable stream.
   * @param outputStream the writable stream.
   * @throws IOException
   */
  public static void streamBin(InputStream inputStream, OutputStream outputStream) throws IOException {
    streamBin(inputStream, outputStream, BUFFER_SIZE, true);
  }
  
  /**
   * Streams from binary to binary with the given buffer size and closes the streams if must close.
   * @param inputStream the readable stream.
   * @param outputStream the writeable stream.
   * @param isClose if true, the input and output streams are closed after streaming.
   * @throws IOException
   */
  public static void streamBin(InputStream inputStream, OutputStream outputStream, boolean isClose) throws IOException {
    streamBin(inputStream, outputStream, BUFFER_SIZE, isClose);
  }
  
  /**
   * Streams from binary to binary with the given buffer size and closes the streams if must close.
   * @param inputStream the readable stream.
   * @param outputStream the writeable stream.
   * @param bufferSize
   * @param isClose if true, the input and output streams are closed after streaming.
   * @throws IOException
   */
  public static void streamBin(InputStream inputStream, OutputStream outputStream, int bufferSize, boolean isClose) throws IOException {
    int count;
    byte[] buffer = new byte[bufferSize];
    while((count = inputStream.read(buffer, 0, bufferSize)) != -1) {
      outputStream.write(buffer, 0,  count);
    }
    if(isClose) {
      inputStream.close();
      outputStream.close();
    }
  }
  
  /**
   * Streams from text to text and closes the streams.
   * @param reader the readable stream.
   * @param writer the writeable stream.
   * @throws IOException
   */
  public static void streamTxt(BufferedReader reader, PrintWriter writer) throws IOException {
    String line;
    while((line = reader.readLine()) != null) {
      writer.println(line);
    }
    reader.close();
    writer.close();
  }
  /**
   * Streams from CSV to HTML and closes the streams.
   * @param inputStream the readable stream.
   * @param outputStream the writeable stream.
   * @param csvSeparator the separator character in the CSV file.
   * @param htmlEncoding the encoding in the HTML file.
   * @throws IOException
   */
  public static void streamCsvToHtml(InputStream inputStream, OutputStream outputStream, char csvSeparator, String htmlEncoding) throws IOException {
    outputStream.write("<!DOCTYPE html>".getBytes());
    outputStream.write("<html>".getBytes());
    outputStream.write("<head>".getBytes());
    outputStream.write(("<meta http-equiv=\"content-type\" content=\"text/html; charset=" + htmlEncoding + "\">").getBytes());
    outputStream.write("<style type=\"text/css\">".getBytes());
    outputStream.write("table {background: #000000}".getBytes());
    outputStream.write("th {background: #E0E0E0}".getBytes());
    outputStream.write("td {background: #FFFFFF}".getBytes());
    outputStream.write("</style>".getBytes());
    outputStream.write("</head>".getBytes());
    outputStream.write("<body><table border=0 cellpadding=2 cellspacing=1> <tr>".getBytes());
    int lineCount = 0;
    List<Byte> bytes =  new ArrayList<>();
    int aByte;
    while((aByte = inputStream.read()) != -1) {
      if(aByte == csvSeparator || aByte == '\n') {
        byte[] byteArray = new byte[bytes.size()];
        for(int i = 0; i < bytes.size(); i++) {
          byteArray[i] = bytes.get(i);
        }
        bytes =  new ArrayList<>();
        
        if(lineCount == 0) { // Header line.
          outputStream.write("<th>".getBytes());
          outputStream.write(byteArray);
          outputStream.write("</th>".getBytes());
        }
        else {
          outputStream.write("<td>".getBytes());
          outputStream.write(byteArray);
          outputStream.write("</td>".getBytes());
        }
        if(aByte == '\n') {
          outputStream.write("</tr> <tr>".getBytes());
          lineCount++;
          if(lineCount == MAX_HTML_LINES) { // Too big html file.
            break;
          }
        }
      }
      else {
        bytes.add((byte)aByte);
      }
    }
    outputStream.write("</tr> </table>".getBytes());
    if(lineCount == MAX_HTML_LINES) {
      outputStream.write("<b color=\"#800000\">File too big! Lines omited in this view!</b>".getBytes());
    }
    outputStream.write("</body> </html>".getBytes());
    
    inputStream.close();
    outputStream.close();
  }
}
