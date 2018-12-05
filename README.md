# Server  

## Setup
Prerequisites: nodejs and npm

* Clone branch
* `npm install`
* rename example-config.json to config.json
* fill config.json with data

## Usage

* `npm start` to start Server

## Config

* servername: MySql server
* database: MySql database
* username: MySql user
* password: MySql password
* hashkey: key for SHA256 encryption
* port: port to start listening for connections

## Packages

* express: Manages Routes of Webserver
* socket-io: Manages communication in game
* mysql: Creates connection to database
* basic-auth: handels authentication for admin panel
* typescript: addes typing to javascript
* source-map-support: changes stacktrace to point to typescript files instead of compiled javascript
* tslint: code style checking for typescript