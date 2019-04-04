package com.tuojie.transport.pc;

import com.tuojie.transport.pc.adb.Command;

import static com.tuojie.transport.pc.Logger.log;

public class ClientException extends RuntimeException {

    public ClientException(String message) {
        this(message, null);
    }

    public ClientException(String message, Command.Result result) {
        super(message);
        String str = result == null ? "" : result.toString();
        log(String.format("ClientException occured: cause => %s\n%s", message, str));
    }
}
