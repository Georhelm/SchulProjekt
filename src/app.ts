import express = require("express");
import {Server} from 'socket.io';
import AdminRouter = require("./admin/Router");
import GameRouter = require("./Router");
import {Config} from "./Config";
import {GameSocket} from "./Socket";
import {DatabaseConnection} from "./DatabaseConnector";
require('source-map-support').install();

const config: Config = require("../config.json");

const app: express.Application = express();

// creates the database connection
new DatabaseConnection(config.serverName, config.username, config.password, config.database);

// defines the routers to use for the webserver
app.use(express.json());

app.use("/", GameRouter.createGameRouter());

app.use("/admin", AdminRouter.createAdminRouter());

// starts the webserver
const server = app.listen(config.port, () => {
	console.log("server started at port " + config.port);
});

// creates the gamesocket
const socket = new GameSocket(server);

socket.init();
