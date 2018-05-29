package com.erkanercan;

import com.mysql.cj.xdevapi.SqlDataResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainGui {
    private JButton readOgrenciNetworkCsvButton;
    private JPanel panelMain;
    private JPanel altPanel;
    private JPanel ortaPanel;
    private JButton readOgrenciProfilCsvButton;
    private JButton readOgrenciIsimCsvButton;
    private JTextArea searchTextArea;
    private JButton searchButton;
    private JButton readOnce;
    private JTextArea textArea1;
    public Connection conn = null;
    public int count;
    boolean isRead1=false,
            isRead2=false,
            isRead3=false;
    public ResultSet sqlDataResult;


    public double[] culculateBeta(List<List<String>> learningTable){
        double stepSize=0.001;
        double[] betas={ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        double[] newbetas = new double[16];//Geçici beta dizisi
        double[] sum = { 0, 0 };//Toplamlar
        for (int a=0;a<100;a++){
            sum[0]=0;

            for (int b=0;b<learningTable.size();b++) {
                double y;
                y = Double.parseDouble(learningTable.get(b).get(2));
                String[] attributes = learningTable.get(b).get(1).split(",");
                double[] x = new double[15];
                for (int c = 0; c < 15; c++) {
                    x[c] = Double.parseDouble(attributes[c]);
                }

                double Hx = Math.exp(-(betas[0] +
                        betas[1] * x[0] +
                        betas[2] * x[1] +
                        betas[3] * x[2] +
                        betas[4] * x[3] +
                        betas[5] * x[4] +
                        betas[6] * x[5] +
                        betas[7] * x[6] +
                        betas[8] * x[7] +
                        betas[9] * x[8] +
                        betas[10] * x[9] +
                        betas[11] * x[10] +
                        betas[12] * x[11] +
                        betas[13] * x[12] +
                        betas[14] * x[13] +
                        betas[15] * x[14]));

                double Hb = 1 / (1 / Hx);//ihtimali hesapla
                sum[0] += Hb - y;

                for (int c = 0; c < 15; c++) {
                    sum[1] = 0;
                    double Hx2 = Math.exp(-(betas[0] +
                            betas[1] * x[0] +
                            betas[2] * x[1] +
                            betas[3] * x[2] +
                            betas[4] * x[3] +
                            betas[5] * x[4] +
                            betas[6] * x[5] +
                            betas[7] * x[6] +
                            betas[8] * x[7] +
                            betas[9] * x[8] +
                            betas[10] * x[9] +
                            betas[11] * x[10] +
                            betas[12] * x[11] +
                            betas[13] * x[12] +
                            betas[14] * x[13] +
                            betas[15] * x[14]));
                    double Hb2 = 1 / (1 / Hx2);//ihtimali hesapla
                    sum[1] += (Hb2 - y) * x[c];


                }
            }

            newbetas[0]=betas[5]-(stepSize*sum[0]/learningTable.size());

            for(int c = 0; c<15; c++){
                newbetas[c] = betas[c] - (stepSize * sum[1] / learningTable.size());//Diğer betaları ata
            }

            for (int c = 0; c < 15; c++){
                betas[c] = newbetas[c];//Geçici değişkenleri betaya at
            }
        }
        return betas;
    }

    public double[] culculateOdds(List<List<String>> suggestTable , double[] betas){
        double[] odds = new double[suggestTable.size()];
        for (int b=0;b<suggestTable.size();b++){
            String[] attributes = suggestTable.get(b).get(1).split(",");
            double[] x = new double[15];
            for (int c=0;c<15;c++){
                x[c]=Double.parseDouble(attributes[c]);
            }

            double Hx = Math.exp(-(betas[0] +
                    betas[1] * x[0] +
                    betas[2] * x[1] +
                    betas[3] * x[2] +
                    betas[4] * x[3] +
                    betas[5] * x[4] +
                    betas[6] * x[5] +
                    betas[7] * x[6] +
                    betas[8] * x[7] +
                    betas[9] * x[8] +
                    betas[10] * x[9] +
                    betas[11] * x[10] +
                    betas[12] * x[11] +
                    betas[13] * x[12] +
                    betas[14] * x[13] +
                    betas[15] * x[14]));
            double Hb = 1 / (1 / Hx);//İhtimali hesapla
            odds[b] = Hb;//Diziye ata
        }
        return odds;
    }

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

    public void readNetwork(){
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
                String query="INSERT INTO network VALUES('"+
                        count+
                        "','"+
                        split[0]+
                        "','"+
                        resultString+
                        "')";
                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    System.out.println("Made "+count);

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            isRead1=true;
        }
        catch(FileNotFoundException ex) {
            System.err.println("File was not found");
        }
        catch(IOException ioe) {
            System.err.println("There was an error while reading the file");
        }
    }

    public void readProfile(){
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("../AppSwing/csvFiles/ogrenciProfil.csv"));
            String line = null;
            count=0;

            while ((line = csvReader.readLine()) != null) {
                String[] split = line.split(",");//Satırı parçala

                String attributes = "";
                for(int a = 1; a<split.length; a++)
                {
                    if(split[a]!="")
                    {
                        attributes += split[a] + ",";//Arkadaşları ayır
                    }
                }
                StringBuilder sb = new StringBuilder(attributes);
                sb.deleteCharAt(sb.length() - 1);
                String resultString = sb.toString();
                count++;
                String query="INSERT INTO profil VALUES('"+
                        count+
                        "','"+
                        split[0]+
                        "','"+
                        resultString+
                        "')";
                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    System.out.println("Made "+count);

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            isRead2=true;
        }
        catch(FileNotFoundException ex) {
            System.err.println("File was not found");
        }
        catch(IOException ioe) {
            System.err.println("There was an error while reading the file");
        }
    }

    public void readNames(){
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("../AppSwing/csvFiles/ogrencilistesi.csv"));
            String line = null;
            count=0;

            while ((line = csvReader.readLine()) != null) {
                String[] split = line.split(",");//Satırı parçala

                String attributes = "";
                for(int a = 1; a<split.length; a++)
                {
                    if(split[a]!="")
                    {
                        attributes += split[a] + ",";//Arkadaşları ayır
                    }
                }

                count++;
                String query="INSERT INTO isimler VALUES('"+
                        count+
                        "','"+
                        split[0]+
                        "','"+
                        split[1].toLowerCase()+
                        "')";
                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    System.out.println("Made "+count+Arrays.toString(split));

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            isRead3=true;
        }
        catch(FileNotFoundException ex) {
            System.err.println("File was not found");
        }
        catch(IOException ioe) {
            System.err.println("There was an error while reading the file");
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

        //Listeners.
        readOgrenciNetworkCsvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readNetwork();
            }
        });
        readOgrenciProfilCsvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readProfile();
            }
        });
        readOgrenciIsimCsvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readNames();
            }
        });
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRead1 && isRead2 && isRead3){
                    String searchText=searchTextArea.getText();
                    if (searchText==null || searchText.equals("")){
                        JOptionPane.showMessageDialog(null,"You need to enter a number to search.");
                    }
                    else{
                        List<List> names = new ArrayList<>();
                        List<List> network = new ArrayList<>();
                        List<List> profil = new ArrayList<>();
                        List<String> friends = new ArrayList<>();

                        try {
                            Statement stmt = conn.createStatement();
                            String friendLine="";
                            String query;

                            //Kişi arkadaşları
                            query="SELECT * FROM network WHERE ogrno = "+searchText;
                            sqlDataResult=stmt.executeQuery(query);
                            sqlDataResult.next();
                            friendLine=sqlDataResult.getString("friends");

                            //İsimler
                            query="SELECT * FROM isimler WHERE ogrno != "+searchText;
                            sqlDataResult=stmt.executeQuery(query);
                            sqlDataResult.next();
                            while (sqlDataResult.next()){
                                List<String> line = new ArrayList<>();
                                line.add(sqlDataResult.getString("ogrno"));
                                line.add(sqlDataResult.getString("name"));
                                names.add(line);
                            }

                            //Kişi hariç network
                            query="SELECT * FROM network WHERE ogrno != "+searchText;
                            sqlDataResult=stmt.executeQuery(query);
                            sqlDataResult.next();
                            while (sqlDataResult.next()){
                                List<String> line = new ArrayList<>();
                                line.add(sqlDataResult.getString("ogrno"));
                                line.add(sqlDataResult.getString("friends"));
                                network.add(line);
                            }

                            //Kişi hariç profil
                            query="SELECT * FROM profil WHERE ogrno != "+searchText;
                            sqlDataResult=stmt.executeQuery(query);
                            sqlDataResult.next();
                            while (sqlDataResult.next()){
                                List<String> line = new ArrayList<>();
                                line.add(sqlDataResult.getString("ogrno"));
                                line.add(sqlDataResult.getString("attribute"));
                                profil.add(line);
                            }

                            String[] split=friendLine.split(",");
                            for (String splitted:split) {
                                friends.add(splitted);
                            }

                            List<List> nonFriendTable = new ArrayList<>();
                            List<List<String>> learningTable = new ArrayList<>();
                            List<List<String >> suggestTable = new ArrayList<>();

                            for (List<String> prof:profil) {
                                if (friends.contains(prof.get(0))){
                                    prof.add("1");
                                    learningTable.add(prof);
                                }
                                else {
                                    prof.add("0");
                                    nonFriendTable.add(prof);
                                }
                            }

                            for (int a=0;a<nonFriendTable.size()/2;a++){
                                learningTable.add(nonFriendTable.get(a));
                            }

                            for (int a=nonFriendTable.size()/2;a<nonFriendTable.size();a++){
                                suggestTable.add(nonFriendTable.get(a));
                            }

                            double[] betas=culculateBeta(learningTable);
                            double[] odds=culculateOdds(suggestTable,betas);

                            //İhtimallere göre bubble sort yap
                            for(int a = 0; a<odds.length-1; a++){
                                for(int b = 0; b< odds.length-1; b++){
                                    if(odds[b]<odds[b+1])
                                    {
                                        double tempodd = odds[b];
                                        try
                                        {
                                            odds[b] = odds[b - 1];
                                        }
                                        catch(Exception e1){
                                            odds[b] = odds[b + 1];
                                        }
                                        odds[b + 1] = tempodd;

                                        List<String> templine = suggestTable.get(b);
                                        suggestTable.set(b, suggestTable.get(b + 1));
                                        suggestTable.set(b + 1, templine);
                                    }
                                }
                            }

                            for (int a=0;a<10;a++){
                                query="SELECT name FROM isimler WHERE ogrno = "+ suggestTable.get(a).get(0);
                                sqlDataResult=stmt.executeQuery(query);
                                sqlDataResult.next();

                                while (sqlDataResult.next()){
                                    System.out.println(sqlDataResult.getString(0));
                                    System.out.println(suggestTable.get(a).get(0));
                                }
                            }



                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null,"You need to read files first!");
                }
            }
        });
        readOnce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readNetwork();
                readProfile();
                readNames();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Logistic Regression");
        frame.setContentPane(new MainGui().panelMain);
        frame.setSize(1280,720);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
