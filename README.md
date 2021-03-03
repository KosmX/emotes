WIP  
What am I doing:  
I'm reimplementing the whole mod to make easier to port it to different modloaders and MC versions  
(It'll need mixin anyway)  
I'll also implement a module to make possible to redirect the server communication to another server.  
For PvP / other clients what are uses an independent server to communicate.  

Modules:
--------
emotecraftCommon: common library used by Emotecraft, not depend on anything  
executor: the interface what will be implemented to the modloader+MC version
emotesMain: Main client-side logic  
emotesServer: Server-side logic  
<br>
fabric: latest fabric implementation  

Other implementation will be in a different branch/repo
-
<br>

oldJunk will be deleted, but I don't want to remove it before the refracting is done
<br>
<br>
Go to 1.4 (emotecraft v1.4) branch to build something what is working....
