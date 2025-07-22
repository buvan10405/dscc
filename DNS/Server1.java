import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class Server1 {
    public static void main(String a[]) {
        ServerSocket sock;
        Socket client;
        DataInputStream input;
        PrintStream ps;
        String url, u, s;
        Connection con = null;
        Statement smt = null;
        ResultSet rs;
        try {
            s = u = "";
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3307/dns", "root", "");
            smt = con.createStatement();
            sock = new ServerSocket(5123);
            System.out.println("Server1 listening on port 5123...");

            while (true) {
                client = sock.accept();
                input = new DataInputStream(client.getInputStream());
                ps = new PrintStream(client.getOutputStream());
                url = input.readLine();
                System.out.println("IN SERVER1 URL IS: " + url);

                StringTokenizer st = new StringTokenizer(url, ".");
                s = "";
                while (st.countTokens() > 1) {
                    s = s + st.nextToken() + ".";
                }
                if (!s.isEmpty()) {
                    s = s.substring(0, s.length() - 1).trim();
                }
                u = st.nextToken();

                rs = smt.executeQuery("select port,ipadd from root where name='" + u + "'");
                if (rs.next()) {
                    ps.println(rs.getString(1));
                    ps.println(rs.getString(2));
                    ps.println(s);
                } else {
                    ps.println("Illegal address please check the spelling again");
                }
                client.close();
            }
        } catch (Exception e) {
            System.err.println("Server1 Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (smt != null) smt.close();
                if (con != null) con.close();
            } catch (SQLException se) {
                System.err.println("Server1 SQL Closing Error: " + se.getMessage());
            }
        }
    }
}