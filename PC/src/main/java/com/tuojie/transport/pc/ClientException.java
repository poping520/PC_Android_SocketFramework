package com.tuojie.transport.pc;

import static com.tuojie.transport.pc.Logger.log;

class ClientException extends RuntimeException {

    ClientException(String message) {
        this(message, null);
    }

    ClientException(String message, Command.Result result) {
        super(message);
        String str = result == null ? "" : result.toString();
        log(String.format("ClientException occured; cause => %s\n%s", message, str));
    }
}
