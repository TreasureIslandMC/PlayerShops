<p align="center"><img src="media/Logo/Main.png" /></p>

PlayerShops allows players to create their own virtual shops where they can sell or purchase items from any player, even if they are offline. Players can also search for items to get the best deals, with a simple and interactive search command. For admins, PlayerShops comes with support for both MySQL and File data storage, along with a transaction logging feature to track any information you need.

<p align="center"><img src="media/Text/Features.png" /></p>

 - Requires Vault
 - Supports MySQL and File data storage (MySQL Recommend)
 - Effective and Efficient
 - Easily remove old accounts
 - Setup custom commands
 - Supports online and offline player shops
 - Update all shops simultaneously
    - All players viewing a shop at the same time will see changes made by any one else
 - Admin Mode for editing shops
 - Set different shop sizes using permissions
 - Creative Block
 - Transaction Logger
    - Control which interactions to log
 - Transaction Bills (fully customization)
 - Search for Items
    - Ordered based on cost
    - Supports interactive chat (spigot only)
 - Taxes
    - Apply when adding items to shop and/or when collecting transaction bill
    - Remove taxes from items less then $x
    - Supports PERCENT, FLAT and NONE
    - Special taxes for items
    - Give player discounts for taxes
 - Ultimate customization
    - Change every message
    - Customize lore/date/number formats & more
 - Signs
    - Open player shops or execute searches
   
   
<p align="center"><img src="media/Text/Commands.png" /></p>

**Aliases: pshop, pshops, ps, playershops**

\<Required>  (Optional)

* /ps - Help menu
* /ps clean (days) - Cleans the database of older accounts
* /ps delete <player> -  Removes a player from the database
* /ps edit <player> - Admin edit mode for editing items in a player shop
* /ps search <ID/Name> - Search player shops for this item
* /ps sell <price> - Adds the item in your hand to your shop
* /ps shop <player> - Opens this players shop
* /ps reload - Reloads everything related to PlayerShops
* /ps setconfig - Set values in the config
* /ps convert <mysql/file> - Converts to a database type
    
<p align="center"><img src="media/Text/Permissions.png" /></p>
* playershops.size.1 - Player with a 1-row inventory shop (default)
* playershops.size.2 - Player with a 2-row inventory shop
* playershops.size.3 - Player with a 3-row inventory shop
* playershops.size.4 - Player with a 4-row inventory shop
* playershops.size.5 - Player with a 5-row inventory shop
* playershops.size.6 - Player with a 6-row inventory shop
* playershops.shop - Access to /ps shop
* playershops.clean - Access to /ps clean
* playershops.search - Access to /ps search
* playershops.edit - Access to /ps edit
* playershops.delete - Access to /ps delete
* playershops.sell - Access to /ps sell
* playershops.reload - Access to /ps reload
* playershops.convert - Access to /ps convert
* playershops.setconfig - Access to /ps setconfig
* playershops.sign.create - Ability to create signs
* playershops.sign.use - Ability to use signs
* playershops.sign.destory - Ability to destory signs.
* playershops.update - Sends a message when update is available
* playershops.admin - Access to anything with a star
    
<p align="center"><img src="media/Text/FQA.png" /></p>

**I don't want a message to show when a player ____ , how do I remove it?**
Set the string in lang.yml to ''.

**Why is the plugin not loading?**
Make sure Vault is installed.

**How do I switch from MySQL to File data storage?**

Method 1 (File Directory)
 - Place the plugin in your plugins folder
 - Start your server to generate the config.yml file
 - Stop your server
 - Go in to the config.yml and under Database Type, set it from MYSQL to FILE
 - Start your server
 - Done!
 
Method 2 (In-game - This method will remove all comments from the config.yml)
 - Type /ps setconfig Database.Type FILE (case sensitive)
 - Then reload PlayerShops /ps reload
 - Done!

**Why won't it connect to MySQL?**
Make sure you have entered the proper information in the config.yml file.

**Can I convert from MySQL to File storage, or vice versa?**
 - MySQL to File: /ps convert file
 - File to MySQL: /ps convert mysql

**Is there support for all languages?**
A file is generated on your local computer called lang.yml. Here you can change any message you like to support any language you like.
