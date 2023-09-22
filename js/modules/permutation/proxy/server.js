const {PORT, CLIENT_HOST, LOG} = require("../../config");
const {EVT_MSG, EVENT_HANDLER, getDefaultKey, EVT_METADATA, EVT_KEY} = require("../encryption");
const {LOG_COLOR} = require("../styling");

const clientKeys = new Map();
const keysMetadata = new Map();
clientKeys.set(-1, getDefaultKey());

const io = require('socket.io')(PORT, {
    cors: {
        origin: [CLIENT_HOST]
    }
});

const handleConnection = socket => {
    LOG_COLOR(`A client has connected (id: ${socket.id}).`, 'green');
    socket.on(EVT_MSG, msg => {
        LOG(`Message from the client (id: ${socket.id}): ${msg}`);
        let key = clientKeys.get(socket.id) || clientKeys.get(-1);
        let decrypted = EVENT_HANDLER.decrypt(msg, key, keysMetadata.get(socket.id));
        LOG('Sending decrypted message back...');
        socket.emit(EVT_MSG, decrypted);
        LOG_COLOR(`Sent to the client (id: ${socket.id}): ${decrypted}`, 'green');
    });
    socket.on(EVT_METADATA, msg => {
        LOG(`Metadata: ${msg}`);
        keysMetadata.set(socket.id, msg);
    });
    socket.on(EVT_KEY, msg => {
        LOG_COLOR(`Key change from client (id: ${socket.id}): ${msg}`, 'magenta');
        clientKeys.set(socket.id, msg);
        LOG_COLOR('Key set.', 'magenta');
    });
};

io.on('connection', handleConnection);
