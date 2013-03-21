package elv.server.io;

import elv.server.Config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

public class PopulationCorrecter {
  public static final DataSource DATA_DB = Config.dataBase(null);//Config.app());

  public static void main(String[] args) throws Exception {
    String sqlString = "select distinct gender, age, settlement from  pop_2004_mid";
    String midString = "select population from pop_2004_mid where gender=? and age=?"
      + " and settlement=?";
    String prevString = "select population from pop_2003 where gender=? and age=?"
      + " and settlement=?";
    try(Connection connection = DATA_DB.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sqlString)) {
      PreparedStatement midStatement = connection.prepareStatement(midString);
      PreparedStatement prevStatement = connection.prepareStatement(prevString);
      while(resultSet.next()) {
        int gender = resultSet.getInt(1);
        int age = resultSet.getInt(2);
        int settlement = resultSet.getInt(3);

        prevStatement.setInt(1, gender);
        prevStatement.setInt(2, age - 1);
        prevStatement.setInt(3, settlement);
        ResultSet prevSet = prevStatement.executeQuery();
        prevSet.next();
        int prevPop = prevSet.getInt(1);

        midStatement.setInt(1, gender);
        midStatement.setInt(2, age);
        midStatement.setInt(3, settlement);
        ResultSet midSet = midStatement.executeQuery();
        int midPop = 0;
        while(midSet.next()) {
          midPop = midSet.getInt(1);
          if(midPop <= prevPop) {
            break;
          }
        }
        
        System.out.println("2004," + gender + "," + age + "," + settlement + "," + midPop);
      }
    } catch(SQLException exc) {
      throw new RuntimeException(exc);
    }
  }
}
