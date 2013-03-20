package elv.server.io;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import net.java.truevfs.access.TPath;
import net.java.truevfs.access.TVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for splitting.
 */
public class MortalitySplitter {
  private static final Logger LOG = LoggerFactory.getLogger(MortalitySplitter.class);
  // Warning!!! The indices of the data columns may VARY by YEAR!
  // Variables: change by YEAR!!! All indices start with 0!!!
  private static final int currentYear = 2011;
  private static final String inputDirName = "e:/ELV/data/mortality/mortality_" + currentYear;
  private static final String inputFileEnding = "1.xls";
  private static final int sheetIdx = 1;
  private static final int firstDataRowIdx = 4;
  private static final String outputFileName = "e:/ELV/data/mortality/" + currentYear + ".csv";

  public static void main(String[] args) throws Exception {
    final TPath input = new TPath(inputDirName);
    final TPath output = new TPath(outputFileName);
    try(final PrintWriter outputWriter = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
      Files.walkFileTree(input, new TreeSplitter(outputWriter));
    }
    TVFS.umount();
  }

  private static int parseInt(String intString) {
    return Integer.parseInt(intString.replaceAll("\\D", ""));
  }

  private static int parseInt(String intString, int defaultInt) {
    if(intString == null || intString.isEmpty()) {
      return defaultInt;
    }
    return Integer.parseInt(intString.replaceAll("\\D", ""));
  }

  public static class TreeSplitter extends SimpleFileVisitor<Path> {
    private final PrintWriter outputWriter;

    public TreeSplitter(PrintWriter outputWriter) {
      this.outputWriter = outputWriter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      if(file.getFileName().toString().endsWith(inputFileEnding)) {
        Sheet sheet = null;
        int rowIdx = -1;
        try {
          Workbook workbook = Workbook.getWorkbook(Files.newInputStream(file));
          sheet = workbook.getSheet(sheetIdx);
          if(sheet != null) {
            LOG.info("SPLIT... sheet <{}>, file {}", sheet.getName(), file.toString());
            for(rowIdx = firstDataRowIdx; rowIdx < sheet.getRows(); rowIdx++) {
              Cell[] cells = sheet.getRow(rowIdx);
              int deathMonth = parseInt(cells[0].getContents(), 0);
              int permanentResidence = parseInt(cells[3].getContents(), 0);
              int effectiveResidence = parseInt(cells[4].getContents(), 0);
              effectiveResidence = effectiveResidence == 0 ? permanentResidence : effectiveResidence;
              int gender = parseInt(cells[5].getContents());

              String birthDate = cells[6].getContents();
              // Correct birthDate if needed
              int birthDateLength = 8;
              for(int i = birthDate.length(); i < birthDateLength; i++) {
                birthDate += "0";
              }
              int birthYear = parseInt(birthDate.substring(0, 4));
              int birthMonth = parseInt(birthDate.substring(4, 6));
              int birthDay = parseInt(birthDate.substring(6, 8));

              int diagnoser = parseInt(cells[7].getContents(), 0);
              int medicalTreatment = parseInt(cells[9].getContents(), 0);

              String diagnosis_1 = cells[10].getContents();
              int bnoCodeLength = 5;
              for(int i = diagnosis_1.length(); i < bnoCodeLength; i++) {
                diagnosis_1 += "_";
              }

              String newLine = currentYear + "," + deathMonth + ",0,0,0," // Death day, hour, minute.
                + birthYear + "," + birthMonth + "," + birthDay + ","
                + permanentResidence + "," + effectiveResidence + "," + gender + ","
                + (currentYear - birthYear) + "," + diagnosis_1 + ",00000,00000,00000,00000,"
                + diagnoser + "," + medicalTreatment;
              outputWriter.println(newLine);
            }
            LOG.info("DONE ::: sheet <{}>, {} rows, file {}", sheet.getName(), rowIdx, file.toString());
          }
        } catch(Exception exc) {
          if(sheet != null && rowIdx >= 0) {
            LOG.error("ERROR!!! sheet <{}>, row {}, file {}", sheet.getName(), rowIdx, file.toString());
          }
          Throwables.propagate(exc);
        }
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
