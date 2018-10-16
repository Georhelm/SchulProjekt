import express = require("express");
import {Server} from 'socket.io';
import {GameRouter} from "./Router";
import {Config} from "./Config";

const config: Config = require("../config.json");

const app: express.Application = express();

app.use("/", GameRouter);

app.listen(config.port, () => {
	console.log("server started at port " + config.port);
	console.log(config.serverName);
});
