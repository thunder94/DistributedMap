package pl.edu.agh.dsrg.sr.protos;

import java.util.Scanner;

public class App {

    public static void main(String args[]) {
        DistributedMap distributedMap = new DistributedMap();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter channel name:");
        String channelName = scanner.nextLine();
        try {
            distributedMap.start(channelName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            String line = scanner.nextLine();
            String[] splitted = line.split("\\s+");

            if(splitted.length == 3) {
                if(splitted[0].equals("put")) {
                    String result = distributedMap.put(splitted[1], splitted[2]);
                    System.out.println("Value was successfully put");
                    if(result != null) {
                        System.out.println("Previous value: "+result);
                    }
                }
            }

            else if(splitted.length == 2) {
                if(splitted[0].equals("get")) {
                    String result = distributedMap.get(splitted[1]);
                    if(result != null) {
                        System.out.println(result);
                    } else {
                        System.out.println("There is no value for this key");
                    }
                }
                else if(splitted[0].equals("contains")) {
                    System.out.println( distributedMap.containsKey(splitted[1]) );
                }
                else if(splitted[0].equals("remove")) {
                    String result = distributedMap.remove(splitted[1]);
                    if(result != null) {
                        System.out.println("Successfully removed");
                    } else {
                        System.out.println("No value for this key");
                    }

                }
            }
        }
    }

}
