package com.tuojie.transport.pc;

class ClientException extends RuntimeException {

    ClientException(String message, Command.Result result) {
        super(message + "\n" + result.toString() + "\n");
    }
}
