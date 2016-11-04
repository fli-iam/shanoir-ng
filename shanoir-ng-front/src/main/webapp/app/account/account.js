"use strict";
var Account = (function () {
    function Account(account) {
        this.authenticated = true;
        if (account) {
            this.authenticated = false;
        }
    }
    return Account;
}());
exports.Account = Account;
//# sourceMappingURL=account.js.map