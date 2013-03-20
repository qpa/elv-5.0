package elv.common.net;

import java.io.Serializable;

/**
 * Request to the net.
 * @author Elv
 */
public class Request implements Serializable {
  /** Serial ID. */
  private static final long serialVersionUID = 7670032133823747641L;

  /** Request type. */
  public enum TYPE {
    /** Request type. */
    LOAD_ALL_DESCENDANTS,
    /** Request type. */
    LOAD_CHILDREN ,
    
    /** Request type. */
    CREATE_FOLDER,
    /** Request type. */
    MOVE_FOLDER,
    /** Request type. */
    COPY_FOLDER,
    /** Request type. */
    DELETE_FOLDER,
    /** Request type. */
    LIST_FOLDER,
    /** Request type. */
    CREATE_FOLDER_AND_ENCODE_BEAN,
    
    /** Request type. */
    GET_EXECUTED_TASKS,
    /** Request type. */
    GET_EXECUTION_PROGRESSES,
    /** Request type. */
    GET_EXECUTION_ERROR,
    /** Request type. */
    STOP_EXECUTION,
    /** Request type. */
    CLEAN_EXECUTION,
    /** Request type. */
    REWIND_EXECUTION,
    
    /** Request type. */
    GET_DATE,
    /** Request type. */
    GET_ENVIRONMENT_VARIABLES,
    /** Request type. */
    DELETE_FILE,
    /** Request type. */
    GET_EXISTING_YEAR_INTERVALS,    
    /** Request type. */
    GET_TABLES;
  }
  
  /** The type of request. */
  private TYPE type;
  /** The argument list of the request. */
  private Object[] arguments;
  
  /**
   * Constructor.
   * @param type the type of request.
   * @param arguments the arguments of the request.
   */
  public Request(TYPE type, Object... arguments) {
    this.type = type;
    this.arguments = arguments;
  }
  
  /**
   * Getter of the request type.
   * @return the type of this request.
   */
  public TYPE getType() {
    return type;
  }

  /**
   * Getter of the arguments.
   * @return the arguments.
   */
  public final Object[] getArguments() {
    return arguments;
  }
}
