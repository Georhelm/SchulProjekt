import express = require("express");
import {Server} from 'socket.io';
import {GameRouter} from "./Router";
import {Config} from "./Config";
import {GameSocket} from "./Socket";

const config: Config = require("../config.json");

const app: express.Application = express();

app.use(express.json());

app.use("/", GameRouter);


const server = app.listen(config.port, () => {
	console.log("server started at port " + config.port);
});


const socket = new GameSocket(server);

socket.init();