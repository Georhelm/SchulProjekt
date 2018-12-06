/**
 * interface for the format of the config file
 */
declare module "*config.json" {
    const serverName: string;
    const port: number;
    const database: string;
    const username: string;
    const password: string;
    const hashkey: string;
}
