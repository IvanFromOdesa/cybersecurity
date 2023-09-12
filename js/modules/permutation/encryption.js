const {ENV_PATH} = require("../config");
const EVT_MSG = 'msg'

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

    decrypt: function (msg, key) {

        const restoreSubstringWithKey = (str, arr) => {
            let res = '';
            for (let i = 0; i < arr.length; i++) {
                res += str.charAt(arr[i] - 1);
            }
            return res;
        }

        return msg.match(RegExp(`.{1,${key.length}}`, 'g'))
            .map(sub => restoreSubstringWithKey(sub, key)).join('');
    }
}

const getDefaultKey = () => {
    require('dotenv').config({path: ENV_PATH});
    return JSON.parse(process.env.PERMUTATION_PB_KEY);
}

module.exports = {
    EVT_MSG,
    EVENT_HANDLER,
    getDefaultKey
}