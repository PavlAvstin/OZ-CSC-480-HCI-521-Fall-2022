# Prerequisites 
- An existing [discord application](https://discord.com/login?redirect_to=%2Fdevelopers%2Fapplications) with a bot (the token for the bot, as well as client id & client secret for the application.)
- A running MySQL server
- JRE 11
- Maven

# Setup
- Moving forward, make sure you are in the `DB_Engine_Networking` directory. Consider this folder the root/parent directory.
## MySQL
- Enter your MySQL shell
- Create a MySQL user with all privileges on all databases. The command to do this is <br/> `CREATE USER 'username'@'localhost' IDENTIFIED BY 'password'; GRANT ALL PRIVILEGES ON *.* TO 'username'@'localhost'; FLUSH PRIVILEGES;` <br/><br/>**Replace username and password with your own. <br/><br/>Replace localhost with the IP the user will be accessing from. If the MySQL server is running on the same machine you can leave it as localhost.<br/><br/>**
## DiscordBot
- In the root of DiscordBot module (folder) create a file called `.env` with the following content:
```.env
DISCORD_BOT_TOKEN=""

MYSQL_URL="jdbc:mysql://localhost:3306/"

MYSQL_INITIALIZATION_USER="OZ_init"
MYSQL_INITIALIZATION_USER_PASSWORD="password"

MYSQL_REST_USER="OZ_rest"
MYSQL_REST_USER_PASSWORD="password"

MYSQL_BOT_USER="OZ_bot"
MYSQL_BOT_USER_PASSWORD="password"
```
Note: *It is recommended to surround the values with quotes to prevent some machines causing issues with special characters.*
- Set `DISCORD_BOT_TOKEN` equal to the token of your discord bot.
- Set `MYSQL_URL` equal to the URL of your MySQL server. localhost being the IP address, then 3306 being the port (default).
- Set `MYSQL_INITIALIZATION_USER` equal to the username of the MySQL user you created earlier.
- Set `MYSQL_INITIALIZATION_USER_PASSWORD` equal to the password of the MySQL user you created earlier.
- Set `MYSQL_REST_USER_PASSWORD` AND `MYSQL_BOT_USER_PASSWORD` to two new, unique passwords. These users will be created. You can change their usernames if you'd like, but it isn't necessary.

## OpenLiberty
- Copy the `.env` file from the DiscordBot module to the root of OpenLiberty module. Remove the `DISCORD_BOT_TOKEN=` line in the OpenLiberty `.env` file.
- Make sure you are in the root of the OpenLiberty module.

CLIENT_ID and CLIENT_SECRET are the client id and client secret of your discord application.

If you are on a UNIX based system, run the following commands: `export DISCORD_CLIENT_ID=CLIENT_ID` and `export DISCORD_CLIENT_SECRET=CLIENT_SECRET`
If you are on a Windows system, follow [this guide](https://www.architectryan.com/2018/08/31/how-to-change-environment-variables-on-windows-10/) to set environment variables.<br/>
# Running DiscordBot & OpenLiberty
- In the root/parent directory of the project run `mvn clean install` to build the project.
- In the root of the DiscordBot module run `mvn exec:java` to run the bot.
- In the root of the OpenLiberty module run `mvn liberty:run` to run OpenLiberty or run it in dev mode with `mvn liberty:dev`.
# Done!