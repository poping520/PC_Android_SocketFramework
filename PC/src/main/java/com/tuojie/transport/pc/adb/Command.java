package com.tuojie.transport.pc.adb;

import com.tuojie.transport.pc.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("all")
public final class Command {

    public static Result exec(String cmd) {
        if (Main.DEBUG) System.out.println("command => " + cmd);

        int result = -1;
        Process process = null;
        BufferedReader succBR = null;
        BufferedReader errorBR = null;
        StringBuilder succMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();

        try {
            process = Runtime.getRuntime().exec(cmd);

            succBR = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorBR = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String s;
            while ((s = succBR.readLine()) != null) {
                succMsg.append(s).append("\n");
            }
            while ((s = errorBR.readLine()) != null) {
                errorMsg.append(s).append("\n");
            }

            result = process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (process != null) process.destroy();
                if (succBR != null) succBR.close();
                if (errorBR != null) errorBR.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Result(cmd, result, succMsg.toString(), errorMsg.toString());
    }

    public static class Result {
        public String command;
        public boolean isSucc;
        public String succMsg;
        public String errorMsg;

        Result(String command, int result, String succMsg, String errorMsg) {
            this.command = command;
            this.isSucc = (result == 0);
            this.succMsg = succMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "command=" + command + "\n" +
                    "message=" + succMsg + " " + errorMsg + "";
        }
    }
}
