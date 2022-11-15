#!/bin/bash
DISCORD_CLIENT_ID=""
DISCORD_CLIENT_SECRET=""
discordBotToken=""
envFile=".env"
username=""
password=""
localhost=""
initpw=""
restpw=""
botpw=""
discordBotPID=""

#echo "Enter Project Path: Path from home(exclude ~) (The location of DB Engine Folder)" 
#read loc
#if ! cd ~/$loc; then 
#	echo ""
#else 
#pwd
#fi

function trap_ctrl_c ()
{
    echo "Shutting down all liberty servers..."
    (
      cd OpenLibertyFrontend
      mvn liberty:stop
    )
    (
      cd OpenLibertyAPI
      mvn liberty:stop
    )
    (
      cd OpenLibertyMPJWT
      mvn liberty:stop
    )
    echo "All liberty servers have been shut down. Goodbye."
    echo "Shutting down Discord Bot." 
    kill -9 discordBotPID
    exit 2
}


#Set values in variable list and skip this step for quick rebuilds
echo "Enter your MySQL username"
read username
echo "Enter your MySQL password"
read password
echo "Enter the IP address for MySQL"
read localhost


#Startup MySQL server
echo "Copy and pa"
echo "CREATE USER '$sqlUser'@'$localhost' IDENTIFIED BY '$sqlPass'; GRANT ALL PRIVILEGES ON *.* TO '$sqlUser'@'$localhost'; FLUSH PRIVILEGES;" > temp.txt
mysql < temp.txt
rm temp.txt

#create DiscordBot .env
cd DiscordBot
echo "DISCORD_BOT_TOKEN=\"$discordBotToken\"" > .env
echo "BASIC_AUTH_USERNAME=\"dilbert\"" >> .env
echo "BASIC_AUTH_PASSWORD=\"dilbert123456\"" >> .env
echo "OPEN_LIBERTY_FQDN=\"http://localhost:9082\"" >> .env
echo "OPEN_LIBERTY_MPJWT=\"http://localhost:9081/\"" >> .env
echo "MYSQL_URL=\"jdbc:mysql://localhost:3306/\"" >> .env
echo "BOT_LIBERTY_USER"="dilbert" >> .env
echo "BOT_LIBERTY_PASSWORD"="dilbertsPassword" >> .env
echo "MYSQL_INITIALIZATION_USER=\"OZ_init\"" >> .env
echo "MYSQL_INITIALIZATION_USER_PASSWORD=\"password\"" >> .env
echo "MYSQL_REST_USER=\"OZ_rest\"" >> .env
echo "MYSQL_REST_USER_PASSWORD=\"password\"" >> .env
echo "MYSQL_BOT_USER=\"OZ_bot\"" >> .env
echo "MYSQL_BOT_USER_PASSWORD=\"password\"" >> .env
cd ..

#create OpenLiberty .env
cd OpenLibertyAPI
echo "DISCORD_BOT_TOKEN=\"$discordBotToken\"" > .env
echo "BASIC_AUTH_USERNAME=\"dilbert\"" >> .env
echo "BASIC_AUTH_PASSWORD=\"dilbert123456\"" >> .env
echo "OPEN_LIBERTY_FQDN=\"http://localhost:9082\"" >> .env
echo "OPEN_LIBERTY_MPJWT=\"http://localhost:9081/\"" >> .env
echo "MYSQL_URL=\"jdbc:mysql://localhost:3306/\"" >> .env
echo "BOT_LIBERTY_USER"="dilbert" >> .env
echo "BOT_LIBERTY_PASSWORD"="dilbertsPassword" >> .env
echo "MYSQL_URL=\"jdbc:mysql://localhost:3306/\"" >> .env
echo "MYSQL_INITIALIZATION_USER=\"OZ_init\"" >> .env
echo "MYSQL_INITIALIZATION_USER_PASSWORD=\"password\"" >> .env
echo "MYSQL_REST_USER=\"OZ_rest\"" >> .env
echo "MYSQL_REST_USER_PASSWORD=\"password\"" >> .env
echo "MYSQL_BOT_USER=\"OZ_bot\"" >> .env
echo "MYSQL_BOT_USER_PASSWORD=\"password\"" >> .env

#export env variables
export DISCORD_CLIENT_ID="$DISCORD_CLIENT_ID"
export DISCORD_CLIENT_SECRET="$DISCORD_CLIENT_SECRET"

#run installation commands
cd ..
mvn clean install

cd DiscordBot
mvn exec:java
discordBotPID=$!

(
  # shellcheck disable=SC2164
  cd OpenLibertyFrontendA
  # export discord tokens
  export DISCORD_CLIENT_ID="1016727734334005348"
  export DISCORD_CLIENT_SECRET="oUq6Kj2bG_JLZF2NE3HtaDxPt04SBj1L"
  mvn liberty:dev &
)

(
  # shellcheck disable=SC2164
  cd OpenLibertyMPJWT
  mvn liberty:dev &
)

(
  # shellcheck disable=SC2164
  cd OpenLibertyAPI
  # export bot credentials
  export BOT_LIBERTY_USER="dilbert"
  export BOT_LIBERTY_PASSWORD="dilbertsPassword"
  mvn liberty:dev &
)

while true
do
  trap trap_ctrl_c 2
done







