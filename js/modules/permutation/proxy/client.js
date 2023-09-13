const io = require('socket.io-client')
const {CLIENT_HOST, STOP_WORD} = require("../../config");
const {EVT_MSG, EVENT_HANDLER, getDefaultKey, EVT_METADATA, EVT_KEY} = require("../encryption");
const readline = require('readline');
const {LOG_COLOR, colours} = require('../styling')

let key = getDefaultKey();
// Compare user input and server response
let clientMsg = '';

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
});

const socket = io(CLIENT_HOST);

const asyncReadLine = (socket) => {
    rl.question(`${colours.fg.yellow}Input the message to the server:\n`, answer => {
        if (answer === STOP_WORD) {
            socket.disconnect();
            return rl.close();
        }
        clientMsg = answer;
        if (clientMsg.startsWith(EVT_KEY)) {
            LOG_COLOR('Key change request...', 'magenta');
            key = EVENT_HANDLER.generateKey(clientMsg);
            LOG_COLOR('Key set.', 'magenta');
            socket.emit(EVT_KEY, key);
            asyncReadLine(socket);
        } else {
            socket.emit(EVT_METADATA, EVENT_HANDLER.createMetaData(clientMsg));
            socket.emit(EVT_MSG, EVENT_HANDLER.encrypt(clientMsg, key));
        }
    });
}

socket.on(EVT_MSG, msg => {
    LOG_COLOR(`Message from the server: ${msg}`, clientMsg === msg ? 'green' : 'red');
    asyncReadLine(socket);
});

socket.on('connect', () => {
    LOG_COLOR('Connected to the server.', 'green');
    asyncReadLine(socket);
});

socket.on('disconnect', () => {
    LOG_COLOR('Disconnected...', 'cyan');
});