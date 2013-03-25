package elv.server.io;

import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import net.java.truevfs.access.TPath;
import net.java.truevfs.access.TVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulationSplitter {
  private static final Logger LOG = LoggerFactory.getLogger(PopulationSplitter.class);
  
  // Warning!!! The indices of the data columns may VARY by YEAR!
  
  // Variables: change by YEAR!!!
  private static final int currentYear = 2011;
  private static final String inputFileName = "e:/ELV/data/population/OKI_2012_ALL.ZIP/OKI_2012_ALL.XLS";
  private static final String outputFileName = "e:/ELV/data/population/" + currentYear + ".csv";
  
  // Indices: all indices start with 0 and may VARY by YEAR!!!
  private static final int maleSheetIdx = 1;
  private static final int femaleSheetIdx = 2;
  private static final int firstDataRowIdx = 2;
  private static final int settlementIdx = 1;
  private static final int firstAgeColumnIdx = 10;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    final TPath input = new TPath(inputFileName);
    final TPath output = new TPath(outputFileName);
    final Map<Integer, Integer> genderSheets = new HashMap<>();
    genderSheets.put(1, maleSheetIdx);
    genderSheets.put(2, femaleSheetIdx);
    try(final InputStream inputStream = Files.newInputStream(input); final PrintWriter outputWriter = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
      Sheet sheet = null;
      int rowIdx = -1;
      try {
        final Workbook workbook = Workbook.getWorkbook(inputStream);
        for(Map.Entry<Integer, Integer> genderSheet : genderSheets.entrySet()) {
          sheet = workbook.getSheet(genderSheet.getValue());
          if(sheet != null) {
            LOG.info("SPLIT... sheet <{}>", sheet.getName());
            for(rowIdx = firstDataRowIdx; rowIdx < sheet.getRows(); rowIdx++) {
              final Cell[] cells = sheet.getRow(rowIdx);
              int settlement = parseInt(cells[settlementIdx].getContents(), 0) / 10;
              settlement = settlement == 9999 ? 0 : settlement;
              for(int i = firstAgeColumnIdx; i < cells.length; i++) {
                final int ageCount = parseInt(cells[i].getContents().replaceAll("\\D", ""));
                outputWriter.println(currentYear + "," + settlement + "," + genderSheet.getKey() + "," + (i - firstAgeColumnIdx) + "," + ageCount);
              }
            }
            LOG.info("DONE ::: sheet <{}>, {} rows", sheet.getName(), rowIdx);
          }
        }
      } catch(Exception exc) {
        if(sheet != null && rowIdx >= 0) {
          LOG.error("ERROR!!! sheet <{}>, row {}", sheet.getName(), rowIdx);
        }
        throw exc;
      }
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
}
