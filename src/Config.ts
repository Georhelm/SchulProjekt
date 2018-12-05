/**
 * interface for the format of the config file
 */
export interface Config {
    serverName: string,
    port: number,
    database: string,
    username: string,
    password: string,
    hashkey: string
}