import express = require("express");
import {Server} from 'socket.io';
import GameRouter = require("./Router");
import {Config} from "./Config";
import {GameSocket} from "./Socket";
import {DatabaseConnection} from "./DatabaseConnector";

const config: Config = require("../config.json");

const app: express.Application = express();

const connection = new DatabaseConnection(config.serverName, config.username, config.password, config.database);

app.use(express.json());

app.use("/", GameRouter.createGameRouter(connection));


const server = app.listen(config.port, () => {
	console.log("server started at port " + config.port);
});


const socket = new GameSocket(server, connection);

socket.init();