package com.tuojie.transport.pc;

class ClientException extends RuntimeException {

    ClientException(String message) {
        Main.log(message);
    }

    ClientException(String message, Command.Result result) {
        Main.log(String.format("%s\n%s\n", message, result.toString()));
    }
}
