# Quality Damage Tooltips for Wurm Unlimited

## Requirements
* Requires [Ago1024's client modloader](https://github.com/ago1024/WurmClientModLauncher/releases/latest) on the client.
* Requires [Ago1024's server modloader](https://github.com/ago1024/WurmServerModLauncher/releases/latest) on the server.
* Requires installation on both the client (to change the tooltip rendering) and on the server 
(to send quality and damage updates without having to 'examine' the object.)

## Installation
* Client Installation
  * Download QualityDamageTooltipsClient.zip
  * Extract QualityDamageTooltipsClient.zip into wurm unlimited client folder 
(for example, C:/Program Files/SteamLibrary/SteamApps/common/Wurm Unlimited/WurmLauncher). 
The QualityDamageTooltipsClient.jar file should end up in 
mods/QualityDamageTooltipsClient/QualityDamageTooltipsClient.jar.
* Server Installation
  * Download QualityDamageTooltipsServer.zip
  * Extract QualityDamageTooltipsServer.zip into wurm unlimited dedicated server folder 
(for example, C:/Program Files/SteamLibrary/SteamApps/common/Wurm Unlimited Dedicated Server). 
The QualityDamageTooltipsServer.jar file should end up in 
mods/QualityDamageTooltipsServer/QualityDamageTooltipsServer.jar.
* Startup
  * Start the Wurm Dedicated Server and Wurm Client and enjoy!

## Effects
* Mousing over a structure such as a fence, wall, etc. or a dropped item (including forges, carts, rafts, etc.) will 
show text below the normal tooltip text in parentheses listing quality and damage for the object. For example, "(QL=56.1321513, Dam=0.5612533)". 
If there is no damage the damage portion of the display will be ommitted.
* If the server has the mod and the client doesn't you will see 4 extra chat tabs with names like ":mod:structure_quality_info" with lots of numbers in them. :)
* If the client has the mod but the server doesn't you will get the initial quality and damage of dropped items (including forges, carts, etc.) when they 
enter range of your player but they will not be updated automatically and structures (fences, walls, bridges, etc.) will not appear. This is because the only information
the unmodded server sends is the initial values.

## Issues
* The client mod does not clean up when items or structures go out of range of the player or are destroyed. Potentially this could lead to 
memory/speed issues if there are huge numbers of items changing in the vicinity of the player but I haven't noticed any issues in testing so far. 
A future update will handle cleaning these up.
