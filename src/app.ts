import express = require("express");
import sourceMap = require("source-map-support");
import * as config from "../config.json";
import AdminRouter = require("./admin/Router");
import {DatabaseConnection} from "./DatabaseConnector";
import GameRouter = require("./Router");
import {GameSocket} from "./Socket";

sourceMap.install();

const app: express.Application = express();

// creates the database connection
const database = new DatabaseConnection(config.serverName, config.username, config.password, config.database);

// defines the routers to use for the webserver
app.use(express.json());

app.use("/", GameRouter.createGameRouter());

app.use("/admin", AdminRouter.createAdminRouter());

// starts the webserver
const server = app.listen(config.port);

// creates the gamesocket
const socket = new GameSocket(server);

socket.init();
