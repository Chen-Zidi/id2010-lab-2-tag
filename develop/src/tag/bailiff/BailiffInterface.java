// BailiffInterface.java
// 2018-08-16/fki Refactored for v13.

package tag.bailiff;

import java.util.List;
import java.util.UUID;

/**
 * This interface is for the Bailiff's clients. The clients are mobile
 * code which move into the Bailiff's JVM for execution.
 */
public interface BailiffInterface
  extends
    java.rmi.Remote {



  /**
   * Returns a string which confirms communication with the Bailiff
   * service instance.
   */
  public String ping()
          throws
          java.rmi.RemoteException;

  /**
   * Returns a property of the Bailiff.
   *
   * @param key The case-insensitive property key to retrieve.
   * @return The property string or null.
   */
  public String getProperty(String key)
          throws
          java.rmi.RemoteException;

  /**
   * The entry point for mobile code.
   * The client sends and object (itself perhaps), a string
   * naming the callback method and an array of arguments which must
   * map against the parameters of the callback method.
   *
   * @param obj  The object (to execute).
   * @param cb   The name of the method to call as the program of obj.
   * @param args The parameters for the callback method. Note that if
   *             the method has a signature without arguments the value of args
   *             should be an empty array. Setting args to null will not work.
   * @throws java.rmi.RemoteException        Thrown if there is an RMI problem.
   * @throws java.lang.NoSuchMethodException Thrown if the proposed
   *                                         callback is not found (which happen if the name is spelled wrong,
   *                                         the number of arguments is wrong or are of the wrong type).
   */
  public void migrate(Object obj, String cb, Object[] args)
          throws
          java.rmi.RemoteException,
          java.lang.NoSuchMethodException;



  //added
  // get players currently in the bailiff
  public List<String> getDexterList() throws java.rmi.RemoteException;

  //added
  // Query each player if they are 'it' or not
  public boolean hasIt() throws java.rmi.RemoteException;

  //added
  // tag a player if it is not "it"
  public boolean tag(String victimUid) throws java.rmi.RemoteException;

  //added
  // remove a player from a baliff
  public void remove(String uid) throws java.rmi.RemoteException;

  //added
  //get a dexter's id from the Bailiff using uid
  //id actually works like tha name here
  public String getDexterId(String uid) throws java.rmi.RemoteException;

  //added
  //get Bailiff uid
  public String getBailiffUid() throws java.rmi.RemoteException;


}