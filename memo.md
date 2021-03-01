# Memo
### How to run
in the folder cd test/bin/pc
1. Run the servers:
````
r1_httpd
r2_rmid
r3_reggie
````

2. Open a few new cmd to run several Bailiffs, 
   one window for one Bailiff:
```
Bailiff
```

3. Open a new cmd to run Dexters. After you run the command, wait for a 
few seconds so that the dexter will migrate to the Bailiff. Then, you can run
   a new dexter in the original window.
```
Dexter -id A //the dexter with id/name A
Dexter -it -id B//the dexter with id/name B, it has the initial tag
```
Make sure that you only give one dexter the 'it' tag.

According to the professor's requirement, we can run 3 Bailiff with 3 Dexters.
For me, I think that 2 Bailiff with 4 Dexter is more suitable.


### Parameters to adjust
In the Dexter.java: 
```
    /**
     * Default sleep time so that we have time to track what it does.
     */
    //private long restraintSleepMs = 5000;
    private long restraintSleepMs = 1000;
```
If you want to see the Dexters run slower, uncomment the first line with 5000, 
and comment the second line with 1000.

you can also ajust the response speed of 'it' Dexter and not 'it' Dexters in the toplevel function
```
//stop a random time 
// 'it' player should response faster, so that he can catch others
      if(it){
         snooze(rnd.nextInt(400));
      }else{
         snooze(rnd.nextInt(800));
      }
```

### Basic Rules
1. 'it' player try to go the bailiff with max number of dexters and catch one to tag.
    If the bailiff with max number of dexters is the current bailiff, the 'it' player stays.
2. Normal players ask the current Bailiff if the 'it' player is in the current Bailiff. 
    If the 'it' player is in the current Bailiff, they escape to other Bailiffs randomly.
3. The 'it' player try their best to not tag itself. (To avoid this, you can run more dexters)   