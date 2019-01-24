package utils;

import jade.core.Agent;

public class LogAgent {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED =   ANSI_RESET+"\u001B[31m";
    public static final String GREEN =      ANSI_RESET+"\u001B[32m";
    public static final String YELLOW =     ANSI_RESET+"\u001B[33m";
    public static final String BLUE =       ANSI_RESET+"\u001B[34m";
    public static final String PURPLE =     ANSI_RESET+"\u001B[35m";
    public static final String CYAN =       ANSI_RESET+"\u001B[36m";
    public static final String B_GREEN =    ANSI_RESET+"\u001B[92m";
    public static final String B_YELLOW =   ANSI_RESET+"\u001B[93m";
    public static final String B_BLUE =     ANSI_RESET+"\u001B[94m";
    public static final String B_MAGENTA =  ANSI_RESET+"\u001B[95m";
    public static final String B_CYAN =     ANSI_RESET+"\u001B[96m";
    public static final String B_WHITE =     ANSI_RESET+"\u001B[80m";

    private static int colorTaken = 0;

    public static final String [] colors = {
            CYAN,
            B_MAGENTA,
            BLUE,
            YELLOW,
            PURPLE,
            B_CYAN,
            B_BLUE};

    private Agent agent;
    private String color;

    public LogAgent(Agent agent, String color) {
        this.agent = agent;
        this.color = color;
    }

    public LogAgent(Agent agent) {
        this.agent = agent;

        synchronized (colors){
            if (colorTaken < colors.length)
                color = colors[colorTaken++];
            else
                color = ANSI_RESET;
        }
    }

    public void log(String log){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(color  +String.format("%-15s",  "["+agent.getLocalName()+"] ") + log+ANSI_RESET);
    }

}
