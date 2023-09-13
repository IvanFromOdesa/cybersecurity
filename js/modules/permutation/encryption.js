const {ENV_PATH} = require("../config");
const EVT_MSG = 'msg';
const EVT_METADATA = 'meta';
const EVT_KEY = '!key';
const WHITESPACE = ' ';
const DOLLAR = '$';
const SEPARATOR = '/';
const HASH = '#';

const EVENT_HANDLER = {

    encrypt : function (msg, key) {

        /* 3 1 4 5 2 6
         * S A M P L E
         *
         * ->
         *
         * A L S M P E
         */

        const permutateWithOrder = (str, arr) => {
            return arr.reduce((a, c, i) => ((a[c - 1] = str[i]), a), []).join("");
        }

        const addGarbageChars = (msg, degree) => {
            const remains = msg.length % degree;
            if (!remains) {
                return msg;
            }

            const chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            const toAdd = Array(1).join().split(',').map(function() { return chars.charAt(Math.floor(Math.random() * chars.length)); }).join('');

            msg += toAdd;
            return addGarbageChars(msg, degree);
        }

        return addGarbageChars(msg.replace(/\s/g, ''), key.length)
            .match(RegExp(`.{1,${key.length}}`, 'g'))
            .map(sub => permutateWithOrder(sub, key)).join('');
    },

    createMetaData : function (msg) {
        const indexesOf = (string, regex) => {
            let match, indexes = [];
            regex = new RegExp(regex);
            while (match = regex.exec(string)) {
                indexes.push(match.index);
            }
            return indexes;
        }
        return DOLLAR.concat(indexesOf(msg, /\s/g).join(SEPARATOR).concat(HASH, msg.length));
    },

    generateKey: function (msg) {
        return msg.substring(EVT_KEY.length + 1).split(WHITESPACE);
    },

    decrypt: function (msg, key, metadata) {

        const restoreSubstringWithKey = (str, arr) => {
            let res = '';
            for (let i = 0; i < arr.length; i++) {
                res += str.charAt(arr[i] - 1);
            }
            return res;
        }

        const insertWhitespace = (msg, index) => {
            return msg.slice(0, index) + " " + msg.slice(index);
        }

        const parseMetadata = (msg, metaData) => {
            const size = metaData.substring(metaData.indexOf(HASH) + 1);
            metaData = metaData.substring(0, metaData.indexOf(HASH));
            if(metaData !== DOLLAR) {
                metaData.slice(1).split(SEPARATOR).forEach(i => msg = insertWhitespace(msg, i));
            }
            return msg.substring(0, size);
        }

        const res = msg.match(RegExp(`.{1,${key.length}}`, 'g')).map(sub => restoreSubstringWithKey(sub, key)).join('');
        return metadata ? parseMetadata(res, metadata) : res;
    }
}

const getDefaultKey = () => {
    require('dotenv').config({path: ENV_PATH});
    return JSON.parse(process.env.PERMUTATION_PB_KEY);
}

module.exports = {
    EVT_MSG,
    EVT_METADATA,
    EVT_KEY,
    EVENT_HANDLER,
    getDefaultKey
}