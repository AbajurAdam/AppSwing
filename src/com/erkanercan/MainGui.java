package com.erkanercan;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;

public class MainGui {
    private JButton buttonMessage;
    private JPanel panelMain;
    private JTextPane textPane;
    private JPanel solPanel;
    private JPanel sagPanel;
    private JPanel altPanel;
    private JPanel ortaPanel;
    private JProgressBar progressBar1;
    public Connection conn = null;
    public int count;

    public void dbConnect(){
        String databaseURL = "jdbc:mysql://localhost:3306/muh4";
        String user = "root";
        String password = "waspon";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(databaseURL, user, password);
            if (conn != null) {
                System.out.println("Connected to the database");
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Could not find database driver class");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("An error occurred. Maybe user/password is invalid");
            ex.printStackTrace();
        }
    }


    public MainGui() {
        dbConnect();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE network; " );
            stmt.executeUpdate("DROP TABLE profil; ");
            stmt.executeUpdate("DROP TABLE isimler;");

            System.out.println("Dropped tables.");

            stmt.executeUpdate("CREATE TABLE network( " +
                    "no INT NOT NULL," +
                    "ogrno VARCHAR(20) NOT NULL," +
                    "friends VARCHAR(300) NOT NULL" +
                    ");");
            stmt.executeUpdate("CREATE TABLE profil( " +
                    "no INT NOT NULL," +
                    "ogrno VARCHAR(20) NOT NULL," +
                    "attribute VARCHAR(300) NOT NULL" +
                    ");");
            stmt.executeUpdate("CREATE TABLE isimler( " +
                    "no INT NOT NULL," +
                    "ogrno VARCHAR(20) NOT NULL," +
                    "name VARCHAR(300) NOT NULL" +
                    ");");
            System.out.println("Tables created again.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        buttonMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                textPane.setText("");
                String fileResult = "";
                try {
                    BufferedReader csvReader = new BufferedReader(new FileReader("../AppSwing/csvFiles/ogrenciNetwork.csv"));
                    String line = null;

                    while ((line = csvReader.readLine()) != null) {
                        String[] split = line.split(",");//Satırı parçala

                        String friends = "";
                        for(int a = 1; a<split.length; a++)
                        {
                            if(split[a]!="")
                            {
                                friends += split[a] + ",";//Arkadaşları ayır
                            }
                        }
                        StringBuilder sb = new StringBuilder(friends);
                        sb.deleteCharAt(sb.length() - 1);
                        String resultString = sb.toString();
                        count++;
                        String query="INSERT INTO network VALUES('"+count+"','"+split[0]+"','"+resultString+"')";
                        try {
                            Statement stmt = conn.createStatement();
                            stmt.executeUpdate(query);
                            System.out.println("Made "+count);

                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                catch(FileNotFoundException ex) {
                    System.err.println("File was not found");
                }
                catch(IOException ioe) {
                    System.err.println("There was an error while reading the file");
                }
                //textPane.setText();

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainGui");
        frame.setContentPane(new MainGui().panelMain);
        frame.setSize(1280,720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
