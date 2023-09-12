const PORT = 8080;
const CLIENT_HOST = 'http://localhost:' + PORT;
const ENV_PATH = '../js/modules/.env';
const STOP_WORD = '!stop';
const LOG = console.log;

module.exports = {
    PORT,
    CLIENT_HOST,
    ENV_PATH,
    STOP_WORD,
    LOG
}