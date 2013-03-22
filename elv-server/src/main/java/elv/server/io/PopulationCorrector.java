package elv.server.io;

import elv.server.Config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

public class PopulationCorrector {
  public static final DataSource DATA_DB = Config.dataBase(null);//Config.app());

  public static void main(String[] args) throws Exception {
    String sqlString = "select distinct age, settlement from  pop_2004_mid";
    String prevString = "select population from pop_2003 where age=? and settlement=?";
    String midString = "select population from pop_2004_mid where age=? and settlement=?";
    try(Connection connection = DATA_DB.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sqlString)) {
      PreparedStatement prevStatement = connection.prepareStatement(prevString);
      PreparedStatement midStatement = connection.prepareStatement(midString);
      while(resultSet.next()) {
        int age = resultSet.getInt(1);
        int settlement = resultSet.getInt(2);
        if(age == 0) {
          System.out.println("2004,1," + age + "," + settlement + ",NOPE");
          continue;
        }

        prevStatement.setInt(1, age - 1);
        prevStatement.setInt(2, settlement);
        ResultSet prevSet = prevStatement.executeQuery();
        prevSet.next();
        int prevPop = prevSet.getInt(1);

        midStatement.setInt(1, age);
        midStatement.setInt(2, settlement);
        ResultSet midSet = midStatement.executeQuery();
        int midPop = 0;
        while(midSet.next()) {
          midPop = midSet.getInt(1);
          if(midPop <= prevPop) {
            break;
          }
        }

        System.out.println("2004,1," + age + "," + settlement + "," + midPop);
      }
    } catch(SQLException exc) {
      throw new RuntimeException(exc);
    }
  }
}
