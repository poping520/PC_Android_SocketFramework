package com.tuojie.transport.pc;

class ClientException extends RuntimeException {

    ClientException(String message) {
        Main.log("ClientException occured cause => " + message);
    }

    ClientException(String message, Command.Result result) {
        Main.log(String.format("ClientException occured cause => %s\n%s\n", message, result.toString()));
    }
}
