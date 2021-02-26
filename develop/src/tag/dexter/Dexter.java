// Dexter.java
// 2018-08-15/fki Refactored from v11

package tag.dexter;

import java.io.*;		// TODO remove the asterisk
import java.util.List;
import java.util.Random;

import net.jini.core.lookup.*;
import net.jini.lookup.*;

import tag.bailiff.BailiffInterface;

/**
 * Dexter jumps around randomly among the Bailiffs. Dexter can be used
 * to test that the system is operating, and as a template for more
 * evolved agents. Since objects of class Dexter move between JVMs, it
 * must be implement the Serializable marker interface.
 */
public class Dexter implements Serializable
{
  /**
   * Identification string used in debug messages.
   */  
  private String id = "anon";
  
  /**
   * Default sleep time so that we have time to track what it does.
   */
  private long restraintSleepMs = 5000;

  /**
   * The jump count variable is incremented each time method topLevel
   * is entered. Its value is printed by the debugMsg routine.
   */
  private int jumpCount = 0;

  /**
   * The default sleep time between subsequent queries of a Jini
   * lookup server.
   */
  private long retrySleep = 20 * 1000; // 20 seconds

  /**
   * The maximum number of results we are interested when asking the
   * Jini lookup server for present bailiffs.
   */
  private int maxResults = 8;
  
  /**
   * The debug flag controls the amount of diagnostic info we put out.
   */
  protected boolean debug = false;

  private boolean it = false;

  public void setIt(boolean b){
    this.it = b;
  }

  public boolean getIt(){
    return this.it;
  }


  /**
   * The string name of the Bailiff service interface, used when
   * querying the Jini lookup server.
   */
  protected static final String bfiName = "tag.bailiff.BailiffInterface";

  /**
   * Dexter uses a Jini ServiceDiscoveryManager to find Bailiffs. The
   * SDM is not serializable so it must recreated each time a Dexter
   * moves to a different Bailiff. By marking the reference variable
   * as transient, we indicate to the compiler that we are aware of
   * that whatever the variable refers to, it will not be serialized.
   */
  protected transient ServiceDiscoveryManager SDM;

  /**
   * This Jini service template is created in Dexter's constructor and
   * used in the topLevel method to find Bailiffs. The service
   * template IS serializable so Dexter only needs to instantiate it
   * once.
   */
  protected ServiceTemplate bailiffTemplate;

  /**
   * Sets the id string of this Dexter.
   * @param id The id string. A null argument is replaced with the
   * empty string.
   */

  private BailiffInterface currentBailiff;

  public void setCurrentBailiff(BailiffInterface bi) {
    this.currentBailiff = bi;
  }

  public BailiffInterface getCurrentBailiff() {
    return this.currentBailiff;
  }

  public void setId(String id) {
    this.id = (id != null) ? id : "";
  }

  public String getId() {
    return this.id;
  }

  private String target;

  /**
   * Sets the restraint sleep duration.
   * @param ms The number of milliseconds in restraint sleep.
   */
  public void setRestraintSleep(long ms) {
    restraintSleepMs = Math.max(0, ms);
  }

  /**
   * Sets the query retry sleep duration.
   * @param ms The number of milliseconds between each query.
   */
  public void setRetrySleep(long ms) {
    retrySleep = Math.max(0, ms);
  }

  /**
   * Sets the maximum number of results accepted from the Jini lookup
   * server.
   * @param n The maximum number of results.
   */
  public void setMaxResults(int n) {
    maxResults = Math.max(0, n);
  }

  /**
   * Sets or clears the global debug flag. When enabled, trace and
   * diagnostic messages are printed on stdout.
   */
  public void setDebug(boolean isDebugged) {
    debug = isDebugged;
  }

  /**
   * Outputs a diagnostic message on standard output. This will be on
   * the host of the launching JVM before Dexter moves. Once he has migrated
   * to another Bailiff, the text will appear on the console of that Bailiff.
   * @param msg The message to print.
   */
  protected void debugMsg (String msg) {
    if (debug)
      System.out.printf("%s(%d):%s%n", id, jumpCount, msg);
  }

  /**
   * Creates a new Dexter. All the constructor needs to do is to
   * instantiate the service template.
   * @throws ClassNotFoundException Thrown if the class for the Bailiff
   * service interface could not be found.
   */
  public Dexter ()
    throws
      java.lang.ClassNotFoundException
  {

    // The Jini service template bailiffTemplate is used to query the
    // Jini lookup server for services which implement the
    // BailiffInterface. The string name of that interface is passed
    // in the bfi argument. At this point we only create and configure
    // the service template, no query has yet been issued.

    bailiffTemplate =
      new ServiceTemplate
	(null,
	 new Class [] {java.lang.Class.forName (bfiName)},
	 null);
  }

  /**
   * Sleep for the given number of milliseconds.
   * @param ms  The number of milliseconds to sleep.
   */
  protected void snooze (long ms) {
    try {
      Thread.currentThread ().sleep (ms);
    }
    catch (java.lang.InterruptedException e) {}
  }

  /**
   * This is Dexter's main program once he is on his way. In short, he
   * gets himself a service discovery manager and asks it about Bailiffs.
   * If the list is long enough, he then selects one randomly and pings it.
   * If the ping returned without a remote exception, Dexter then tries
   * to migrate to that Bailiff. If the ping or the migration fails, Dexter
   * gives up on that Bailiff and tries another.
   */
  public void topLevel ()
    throws
      java.io.IOException
  {
    jumpCount++;
    
    Random rnd = new Random ();

    // Create a Jini service discovery manager to help us interact with
    // the Jini lookup service.
    SDM = new ServiceDiscoveryManager (null, null);

    // Loop forever until we have successfully jumped to a Bailiff.
    for (;;) {

      ServiceItem [] svcItems;	// holds results from the Jini lookup server

      long retryInterval = 0;	// incremented when no Bailiffs are found

      // if isIt
      if(it){
        // try to tag target.
        if (currentBailiff != null){
          boolean tagSuccess = currentBailiff.tag(target);
          // if tag successful
          if(tagSuccess){
            // isIt = false
            it = false;
             System.out.println(id + " IS NOT 'it' ANYMORE");
          }
        }

      }

      // Sleep a bit so that humans can keep up.
      
      debugMsg("Is here - entering restraint sleep.");
      snooze(restraintSleepMs);
      debugMsg("Leaving restraint sleep.");

      // Try to find Bailiffs using the Jini lookup service.
      // The loop keeps going until we get a non-empty response.
      // If no results are found, we sleep a bit between attempts.

      do {

	if (0 < retryInterval) {
	  debugMsg("No Bailiffs detected - sleeping.");
	  snooze(retryInterval);
	  debugMsg("Waking up, looking for Bailiffs.");
	}

	// Put our query, expressed as a service template, to the Jini
	// service discovery manager. 

	svcItems = SDM.lookup (bailiffTemplate, maxResults, null);
	retryInterval = retrySleep;

	// If no lookup servers or bailiffs are found, go back up to
	// the beginning of the loop, sleep a bit, and then try again.

      } while (svcItems.length == 0);

      // Now, at least one Bailiff has been found.

      debugMsg("Found " + svcItems.length + " Bailiffs");

      // Enter a loop in which we:
      // - randomly pick one Bailiff
      // - pings it to see if it is alive
      // - migrate to it, or try another one

      int nofItems = svcItems.length; // nof items remaining

      while (0 < nofItems) {

          // Randomly pick one of the remaining entries
          int idx = rnd.nextInt(nofItems);

          boolean accepted = false;        // Assume it will fail
          Object obj = svcItems[idx].service; // Get the service object
          BailiffInterface bfi = null;

          debugMsg("Trying to ping...");

          if (obj instanceof BailiffInterface) {
              bfi = (BailiffInterface) obj;
              try {
                  String response = bfi.ping(); // Ping it
                  debugMsg(response);
                  accepted = true;    // It worked!
              } catch (java.rmi.RemoteException rex) {
                  debugMsg("Ping fail: " + bfi);
              }
          }
          // if I am it:
          if (it) {
              List<String> ids = bfi.getPlayerList();
              // ask if bailiff is empty
              if(ids.size() > 0){

                  if (ids.size() == 1 && ids.contains(id)) {
                      accepted = false;
                  } else {
                      for (int i = 0; i < ids.size(); i++) {
                          System.out.println("IDS: in bailiff" + i + " " + ids.get(i));

                          if (!ids.get(i).equals(id)) {
                              target = ids.get(i);
                              break;
                          }
                      }
                  }
              } else {
                  accepted = false;
              }
        }else{
              // ask if in bailiff has it
              boolean hasIt = bfi.hasIt();
              if(hasIt) accepted = false;
          }

	debugMsg(accepted ? "Accepted." : "Not accepted.");

	// If the ping failed, remove that entry from the list.
	// Otherwise, go ahead and attempt the jump.

	if (!accepted) {
	  svcItems[idx] = svcItems[--nofItems];
	}
	else {

	  debugMsg("Trying to jump...");

	  try {
	      if(currentBailiff != null){
	          currentBailiff.remove(id);
          }

	    bfi.migrate(this, "topLevel", new Object [] {});
	    // SUCCESS
	    SDM.terminate();	// shut down Service Discovery Manager
	    return;		// return and end this thread
	  }
	  catch (java.rmi.RemoteException rex) {
	    if (debug) 
	      rex.printStackTrace();
	  }
	  catch (java.lang.NoSuchMethodException nmx) {
	    if (debug)
	      nmx.printStackTrace();
	  }

	  debugMsg("Jump failed!");
	}
      }	// while candidates remain

      debugMsg("All Bailiffs failed.");
    } // for ever
  }   // topLevel

  private static void showUsage() {
    String [] msg = {
      "Usage: {?,-h,-help}|[-debug][-id string][-rs ms][-qs ms][-mr n]",
      "? -h -help   Show this text",
      "-debug       Enable trace and diagnostic messages",
      "-id  string  Set the id string printed by debug messages",
      "-rs  ms      Set the restraint sleep in milliseconds",
      "-qs  ms      Set the Jini lookup query retry delay",
      "-mr  n       Set the Jini lookup query max results limit",
            "-it     initiate it"
    };
    for (String s : msg)
      System.out.println(s);
  }

  // The main method is only used by the initial launch. After the
  // first jump, Dexter always restarts in method topLevel.
  
  public static void main (String [] argv)
    throws
      java.io.IOException, java.lang.ClassNotFoundException
  {

    // Make a new Dexter and configure it from commandline arguments.
    
    Dexter dx = new Dexter();

    // Parse and act on the commandline arguments.

    int state = 0;

    for (String av : argv) {

      switch (state) {

      case 0:
	if (av.equals("?") || av.equals("-h") || av.equals("-help")) {
	  showUsage();
	  return;
	}
	else if (av.equals("-debug"))
	  dx.setDebug(true);
	else if (av.equals("-id"))
	  state = 1;
	else if (av.equals("-rs"))
	  state = 2;
	else if (av.equals("-qs"))
	  state = 3;
	else if (av.equals("-mr"))
	  state = 4;
	else if(av.equals("-it"))
	    dx.setIt(true);
	else {
	  System.err.println("Unknown commandline argument: " + av);
	  return;
	}
	break;

      case 1:
	dx.setId(av);
	state = 0;
	break;

      case 2:
	dx.setRestraintSleep(Long.parseLong(av));
	state = 0;
	break;

      case 3:
	dx.setRetrySleep(Long.parseLong(av));
	state = 0;
	break;

      case 4:
	dx.setMaxResults(Integer.parseInt(av));
	state = 0;
	break;
      }	// switch
    }	// for all commandline arguments

    dx.topLevel();		// Start the Dexter

  } // main
}
