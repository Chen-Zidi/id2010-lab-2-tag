// Dexter.java
// 2018-08-15/fki Refactored from v11

package tag.dexter;

import java.io.*;        // TODO remove the asterisk
import java.rmi.RemoteException;
import java.util.*;

import net.jini.core.lookup.*;
import net.jini.lookup.*;

import tag.bailiff.Bailiff;
import tag.bailiff.BailiffInterface;

/**
 * Dexter jumps around randomly among the Bailiffs. Dexter can be used
 * to test that the system is operating, and as a template for more
 * evolved agents. Since objects of class Dexter move between JVMs, it
 * must be implement the Serializable marker interface.
 */
public class Dexter implements Serializable {
    /**
     * Identification string used in debug messages.
     */
    private String id = "anon";

    /**
     * Default sleep time so that we have time to track what it does.
     */
    //private long restraintSleepMs = 5000;
    private long restraintSleepMs = 1000;

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

    //added
    //if I am it
    private boolean it = false;

    //added
    //unique uid
    private UUID uid = UUID.randomUUID();

    //added
    //choosen victim if I am 'it'
    private String victim;

    //current Bailiff
    private BailiffInterface myBailiff;

    //calculate how many times I migrate
    private int migrateCounter = 0;

    //getter of migrateCounter
    public int getMigrateCounter(){
        return migrateCounter;
    }

    //increase counter by 1
    public void incMigrateCounter(){
        migrateCounter ++;
    }

    //getter of uid
    public String getUId() {
        return this.uid.toString();
    }


    //whe player being tagged
    //set myself to be 'it'
    public boolean setMyItTag() {
        //System.out.println(id + ": get tag request");
        if (!it)/* tag succeeded */ {
            // update my state to be 'it'
            it = true;
            System.out.println(id +"("+ jumpCount +")"+": I am it!");
            return true;
        }
        return false;
    }

    //getter of it
    public boolean getIt() {
        return this.it;
    }

    //setter of it
    public void setIt(boolean b) {
        it = b;
    }

    //added
    //set my Bailiff
    public void setMyBailiff(BailiffInterface b) {
        this.myBailiff = b;
    }

    //added
    //get my Bailiff
    public BailiffInterface getMyBailiff() {
        return this.myBailiff;

    }

    //added
    //to get id
    public String getId() {
        return id;
    }

    //added
    //try to tag victim if I am it
    public void tagVictim() throws RemoteException {

        // try to tag a victim
        if (myBailiff != null) {
            List<String> dexters = myBailiff.getDexterList();
            //choose a victim
            double random = Math.random();
            victim = dexters.get((int)random * dexters.size());
            //if there more dexters try to not tag myself
            if (victim.equals(uid.toString()) && dexters.size() > 1) victim = dexters.get(1);

            System.out.println(id +"("+ jumpCount +")"+ ": I passes tag to " + myBailiff.getDexterId(victim));
            boolean catchVictim = myBailiff.tag(victim);

            if (catchVictim) {//if I caught a victim

                it = false;
                System.out.println(id +"("+ jumpCount +")"+ ": I am not 'it' ");

            }


        }
    }

    /**
     * Sets the id string of this Dexter.
     *
     * @param id The id string. A null argument is replaced with the
     *           empty string.
     */
    public void setId(String id) {
        this.id = (id != null) ? id : "";
    }

    /**
     * Sets the restraint sleep duration.
     *
     * @param ms The number of milliseconds in restraint sleep.
     */
    public void setRestraintSleep(long ms) {
        restraintSleepMs = Math.max(0, ms);
    }

    /**
     * Sets the query retry sleep duration.
     *
     * @param ms The number of milliseconds between each query.
     */
    public void setRetrySleep(long ms) {
        retrySleep = Math.max(0, ms);
    }

    /**
     * Sets the maximum number of results accepted from the Jini lookup
     * server.
     *
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
     *
     * @param msg The message to print.
     */
    protected void debugMsg(String msg) {
        if (debug)
            System.out.printf("%s(%d):%s%n", id, jumpCount, msg);
    }

    /**
     * Creates a new Dexter. All the constructor needs to do is to
     * instantiate the service template.
     *
     * @throws ClassNotFoundException Thrown if the class for the Bailiff
     *                                service interface could not be found.
     */
    public Dexter()
            throws
            java.lang.ClassNotFoundException {

        // The Jini service template bailiffTemplate is used to query the
        // Jini lookup server for services which implement the
        // BailiffInterface. The string name of that interface is passed
        // in the bfi argument. At this point we only create and configure
        // the service template, no query has yet been issued.

        bailiffTemplate =
                new ServiceTemplate
                        (null,
                                new Class[]{java.lang.Class.forName(bfiName)},
                                null);

        myBailiff = null;

    }

    /**
     * Sleep for the given number of milliseconds.
     *
     * @param ms The number of milliseconds to sleep.
     */
    protected void snooze(long ms) {
        try {
            Thread.currentThread().sleep(ms);
        } catch (java.lang.InterruptedException e) {
        }
    }

    //added
    //get the bailiff with max number of Dexter
    public BailiffInterface getMaxNumBailiff(ServiceItem[] svcItems) throws RemoteException {
        int nofItems = svcItems.length;
        //Bailiff and their dexter list numbers
        List<BailiffInterface> bailiffs = new ArrayList<>();

        for (int i = 0; i < nofItems; i++) {
            Object obj = svcItems[i].service;
            BailiffInterface bfi = null;
            if (obj instanceof BailiffInterface) {
                bfi = (BailiffInterface) obj;
                bailiffs.add(bfi);
            }
        }
        //System.out.println(id + ": bailiff list length " + bailiffs.size());

        BailiffInterface maxBailiff = bailiffs.get(0);
        int max = maxBailiff.getDexterList().size();
        for (int i = 0; i < bailiffs.size(); i++) {
            BailiffInterface b = bailiffs.get(i);
            int size = b.getDexterList().size();
            if (max <= size) {
                max = size;
                maxBailiff = b;
            }
        }

        //this is the bailiff with the max dexters,
        //If I am 'it', I want to go to there and find a victim
        System.out.println(id +"("+ jumpCount +")"+ ": max bailiff " + maxBailiff.getBailiffUid());
        return maxBailiff;
    }


    /**
     * This is Dexter's main program once he is on his way. In short, he
     * gets himself a service discovery manager and asks it about Bailiffs.
     * If the list is long enough, he then selects one randomly and pings it.
     * If the ping returned without a remote exception, Dexter then tries
     * to migrate to that Bailiff. If the ping or the migration fails, Dexter
     * gives up on that Bailiff and tries another.
     */
    public void topLevel()
            throws
            java.io.IOException, InterruptedException {
        jumpCount++;

        Random rnd = new Random();

        // Create a Jini service discovery manager to help us interact with
        // the Jini lookup service.
        SDM = new ServiceDiscoveryManager(null, null);

        //System.out.println(id + ": " + uid);


        // Loop forever until we have successfully jumped to a Bailiff.
        for (; ; ) {

            ServiceItem[] svcItems;    // holds results from the Jini lookup server

            long retryInterval = 0;    // incremented when no Bailiffs are found

            //try to tag a victim if I am 'it'
            //if(it) tagVictim();

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

                svcItems = SDM.lookup(bailiffTemplate, maxResults, null);
                retryInterval = retrySleep;

                // If no lookup servers or bailiffs are found, go back up to
                // the beginning of the loop, sleep a bit, and then try again.

            } while (svcItems.length == 0);

            // Now, at least one Bailiff has been found.

            debugMsg("Found " + svcItems.length + " Bailiffs");
            int nofItems = svcItems.length; // nof items remaining

           // getMaxNumBailiff(svcItems);


            BailiffInterface bfiTemp = null;

            //stop a random time
            // 'it' player should response faster, so that he can catch others
            if(it){
                snooze(rnd.nextInt(400));
            }else{
                snooze(rnd.nextInt(800));
            }


            //added
            //if I am not 'it' and I am not in any Bailiff
            if (!it && myBailiff == null) {

                while (0 < nofItems) {

                    // Randomly pick one of the remaining entries
                    int idx = rnd.nextInt(nofItems);
                    boolean accepted = false;


                    Object obj = svcItems[idx].service; // Get the service object

                    bfiTemp = (BailiffInterface) obj;

                    debugMsg("Trying to ping...");

                    try {
                        String response = bfiTemp.ping();
                        debugMsg(response);
                        accepted = true;
                    } catch (java.rmi.RemoteException rex) {
                        debugMsg("Ping fail: " + bfiTemp);
                    }

                    debugMsg(accepted ? "Accepted." : "Not accepted.");

                    // If the ping failed, remove that entry from the list.
                    // Otherwise, go ahead and attempt the jump.

                    if (!accepted) {
                        svcItems[idx] = svcItems[--nofItems];
                    } else {

                        debugMsg("Trying to jump...");

                        try {
                            if (myBailiff != null) {
                                myBailiff.remove(uid.toString());
                            }

                            bfiTemp.migrate(this, "topLevel", new Object[]{});
                            // SUCCESS
                            SDM.terminate();    // shut down Service Discovery Manager
                            return;        // return and end this thread
                        } catch (java.rmi.RemoteException rex) {
                            if (debug)
                                rex.printStackTrace();
                        } catch (java.lang.NoSuchMethodException nmx) {
                            if (debug)
                                nmx.printStackTrace();
                        }

                        debugMsg("Jump failed!");
                    }
                }    // while candidates remain

                debugMsg("All Bailiffs failed.");
            } else if (!it && myBailiff != null){//if I am not it, check my Bailiff does not have it dexter

                //check my Bailiff has it
                boolean danger = myBailiff.hasIt();

                if(!danger) continue;//if not dangerous, just stay

                int idx = rnd.nextInt(nofItems);
                Object obj = svcItems[idx].service; // Get the service object
                bfiTemp = (BailiffInterface) obj;

                while(bfiTemp.getBailiffUid().equals(myBailiff.getBailiffUid())){
                   idx = rnd.nextInt(nofItems);
                   obj = svcItems[idx].service; // Get the service object
                   bfiTemp = (BailiffInterface) obj;
                }//not enter current Bailiff

                while (0 < nofItems) {//try to get one Bailiff to ping

                    boolean accepted = false;
                    debugMsg("Trying to ping...");

                    try {
                        String response = bfiTemp.ping();
                        debugMsg(response);
                        accepted = true;
                    } catch (java.rmi.RemoteException rex) {
                        debugMsg("Ping fail: " + bfiTemp);
                    }

                    debugMsg(accepted ? "Accepted." : "Not accepted.");

                    // If the ping failed, remove that entry from the list.
                    // Otherwise, go ahead and attempt the jump.

                    if (!accepted) {//if fail to ping the bailiff with the max number of dexter
                        // Randomly pick one of the remaining entries
                        idx = rnd.nextInt(nofItems);
                        svcItems[idx] = svcItems[--nofItems];
                    } else {

                        debugMsg("Trying to jump...");

                        try {
                            if (myBailiff != null) {
                                myBailiff.remove(uid.toString());
                            }


                            bfiTemp.migrate(this, "topLevel", new Object[]{});
                            // SUCCESS
                            SDM.terminate();    // shut down Service Discovery Manager
                            return;        // return and end this thread
                        } catch (java.rmi.RemoteException rex) {
                            if (debug)
                                rex.printStackTrace();
                        } catch (java.lang.NoSuchMethodException nmx) {
                            if (debug)
                                nmx.printStackTrace();
                        }

                        debugMsg("Jump failed!");
                    }
                }    // while candidates remain
                debugMsg("All Bailiffs failed.");


            } else if(it){//if I am it, I find the Bailiff with max number of dexter to migrate

                bfiTemp = getMaxNumBailiff(svcItems);

                if(myBailiff != null){
                    //System.out.println("my Bailiff: " + myBailiff.getBailiffUid());
                    //System.out.println("max Bailiff: " + bfiTemp.getBailiffUid());
                    //System.out.println(id +"("+ jumpCount +")"+ ": I am 'it' ");
                    if(myBailiff.getBailiffUid().equals(bfiTemp.getBailiffUid())){
                        tagVictim();
                        continue;
                    }
                }

                while (0 < nofItems) {//try to get one Bailiff to ping

                    boolean accepted = false;
                    debugMsg("Trying to ping...");

                    try {
                        String response = bfiTemp.ping();
                        debugMsg(response);
                        accepted = true;
                    } catch (java.rmi.RemoteException rex) {
                        debugMsg("Ping fail: " + bfiTemp);
                    }

                    debugMsg(accepted ? "Accepted." : "Not accepted.");

                    // If the ping failed, remove that entry from the list.
                    // Otherwise, go ahead and attempt the jump.

                    if (!accepted) {//if fail to ping the bailiff with the max number of dexter
                        // Randomly pick one of the remaining entries
                        int idx = rnd.nextInt(nofItems);
                        svcItems[idx] = svcItems[--nofItems];
                    } else {

                        debugMsg("Trying to jump...");

                        try {
                            if (myBailiff != null) {
                                myBailiff.remove(uid.toString());
                            }

                            System.out.println(id +"("+ jumpCount +")"+ ": move to a Bailiff with max number of dexters ");
                            bfiTemp.migrate(this, "topLevel", new Object[]{});
                            // SUCCESS
                            SDM.terminate();    // shut down Service Discovery Manager
                            return;        // return and end this thread
                        } catch (java.rmi.RemoteException rex) {
                            if (debug)
                                rex.printStackTrace();
                        } catch (java.lang.NoSuchMethodException nmx) {
                            if (debug)
                                nmx.printStackTrace();
                        }

                        debugMsg("Jump failed!");
                    }
                }    // while candidates remain
                debugMsg("All Bailiffs failed.");

            }

        } // for ever
    }   // topLevel

    private static void showUsage() {
        String[] msg = {
                "Usage: {?,-h,-help}|[-debug][-id string][-rs ms][-qs ms][-mr n]",
                "? -h -help   Show this text",
                "-debug       Enable trace and diagnostic messages",
                "-id  string  Set the id string printed by debug messages",
                "-rs  ms      Set the restraint sleep in milliseconds",
                "-qs  ms      Set the Jini lookup query retry delay",
                "-mr  n       Set the Jini lookup query max results limit",
                "-it          initiate it tag"//edited
        };
        for (String s : msg)
            System.out.println(s);
    }

    // The main method is only used by the initial launch. After the
    // first jump, Dexter always restarts in method topLevel.

    public static void main(String[] argv)
            throws
            java.io.IOException, java.lang.ClassNotFoundException, InterruptedException {

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
                    } else if (av.equals("-debug"))
                        dx.setDebug(true);
                    else if (av.equals("-id"))
                        state = 1;
                    else if (av.equals("-rs"))
                        state = 2;
                    else if (av.equals("-qs"))
                        state = 3;
                    else if (av.equals("-mr"))
                        state = 4;
                    else if (av.equals("-it"))
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
            }    // switch
        }    // for all commandline arguments

        dx.topLevel();        // Start the Dexter

    } // main
}
