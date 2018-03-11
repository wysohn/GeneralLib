package org.generallib.deeplearning.rnn;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.jblas.DoubleMatrix;

public class Test {
    public static void main(String[] ar) {
        File file = new File("ChatData.db");
        System.out.println(file.getAbsolutePath());

        Set<String> messageHashes = new HashSet<>();
        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:"+file.getAbsolutePath());){
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM chatdata");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                String message = rs.getString("message");
                message = trimMessage(message);
                if(message.length() < 5)
                    continue;

                messageHashes.add(message);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Read "+messageHashes.size()+" unique messages.");

        Set<String> wordset = new HashSet<>();
/*        for(String message : messageHashes) {
            message = trimMessage(message);
            if(message.length() < 5)
                continue;

            for(String split : message.split(" "))
                wordset.add(split);
        }
        */
        File keywordfile = new File("Keywords.txt");
        Scanner sc;
        try {
            sc = new Scanner(keywordfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        while(sc.hasNextLine())
            wordset.add(sc.nextLine());
        wordset.remove("");
        List<String> wordlist = new ArrayList<>(wordset);

        System.out.println("Vocab size is "+wordlist.size());

        List<int[]> vectors = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        for(String message : messageHashes) {
            boolean zeroVector = true;
            int[] vector = new int[wordlist.size()];
            for(int i = 0; i < message.length(); i++) {
                for(int j = 0; j < wordlist.size(); j++) {
                    Pattern p = Pattern.compile(wordlist.get(j));
                    Matcher m = p.matcher(message);
                    while(m.find()) {
                        zeroVector = false;
                        vector[j] += 1;
                        break;
                    }
                }
            }

            if(!zeroVector) {
                vectors.add(vector);
                messages.add(message);
            }
        }
        messageHashes = null;

        double[][] input = new double[vectors.size()][];
        for(int i = 0; i < input.length; i++) {
            int[] vector = vectors.get(i);
            input[i] = new double[vector.length];

            for(int j = 0; j < vector.length; j++)
                input[i][j] = vector[j];
        }

        KMean kmean = new KMean((int) (wordlist.size() * 1.5), new DoubleMatrix(input));

        for(int i = 0; i < 10; i++) {
            System.out.println(kmean.doTraining(true));
        }

        int[] c = kmean.getClusterIndexes();

        Map<Integer, List<String>> groups = new HashMap<>();
        for(int i = 0; i < messages.size(); i++) {
            int clusterIndex = c[i];
            List<String> group = groups.get(clusterIndex);
            if(group == null) {
                group = new ArrayList<>();
                groups.put(clusterIndex, group);
            }

            group.add(messages.get(i));
        }

        for(Entry<Integer, List<String>> entry : groups.entrySet()) {
            System.out.println("\n\nGROUP "+entry.getKey()+": \n");
            int count = 0;
            for(String str : entry.getValue()) {
                System.out.print(str+", ");
                if(++count % 10 == 0) {
                    System.out.println();
                }
            }
        }
    }

    public static String trimMessage(String message) {
        message = ChatColor.stripColor(message).replaceAll("[^a-zA-Z0-9ㄱ-힣 ]", "").trim();
        return message;
    }
}
